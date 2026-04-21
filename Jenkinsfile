pipeline {
    agent any

    tools {
        maven 'Local Maven 3.9.11'
        jdk 'Local JDK-21'
    }

    environment {
        JAVA_HOME = tool name: 'Local JDK-21', type: 'jdk'
        MAVEN_HOME = tool name: 'Local Maven 3.9.11', type: 'maven'

        // Docker Configuration
        DOCKER_HUB_USER = 'mkiavash'
        IMAGE_NAME = 'shopping-cart-calc'
        IMAGE_TAG = "${env.BUILD_NUMBER}"

        // SonarQube Configuration (Direct approach - no Jenkins server config needed)
        SONAR_HOST_URL = 'http://localhost:9000'
        SONAR_PROJECT_KEY = 'SWEP-KIA'
        SONAR_PROJECT_NAME = 'SWEP-KIA'
        // SONAR_TOKEN will be injected from Jenkins credentials
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                script {
                    def gitBranch = env.GIT_BRANCH ?: 'unknown'
                    def gitCommit = env.GIT_COMMIT ?: 'unknown'
                    echo "Building branch: ${gitBranch}"
                    echo "Commit: ${gitCommit}"
                }
            }
        }

        stage('Setup') {
            steps {
                echo 'Setting up build environment...'
                bat 'java -version'
                bat 'mvn -version'
                bat 'mvn clean'
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling source code...'
                bat 'mvn compile -Dmaven.test.skip=true'
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests with JaCoCo coverage...'
                bat 'mvn test jacoco:report'
            }
            post {
                success {
                    junit '**/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Coverage Report') {
            steps {
                echo 'Generating coverage report...'
                jacoco(
                    execPattern: 'target/jacoco.exec',
                    classPattern: 'target/classes',
                    sourcePattern: 'src/main/java',
                    exclusionPattern: '**/ShoppingCartCalculator.class'
                )
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application...'
                bat 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                // Using credentials plugin to inject SONAR_TOKEN securely
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    bat '''
                        mvn sonar:sonar ^
                            -Dsonar.projectKey=%SONAR_PROJECT_KEY% ^
                            -Dsonar.projectName="%SONAR_PROJECT_NAME%" ^
                            -Dsonar.host.url=%SONAR_HOST_URL% ^
                            -Dsonar.login=%SONAR_TOKEN% ^
                            -Dsonar.sources=src/main/java ^
                            -Dsonar.java.binaries=target/classes ^
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo 'Checking SonarQube Quality Gate result...'
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    script {
                        timeout(time: 5, unit: 'MINUTES') {
                            retry(30) {
                                sleep(time: 10, unit: 'SECONDS')

                                def statusCode = bat(
                                    script: '''
                                        powershell -NoProfile -Command "
                                        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

                                        $headers = @{
                                            'Authorization' = 'Bearer %SONAR_TOKEN%'
                                        }

                                        try {
                                            $response = Invoke-RestMethod `
                                                -Uri 'http://localhost:9000/api/qualitygates/project_status?projectKey=shopping-cart-calculator' `
                                                -Headers $headers `
                                                -ErrorAction Stop

                                            $qgStatus = $response.projectStatus.status
                                            Write-Host "Quality Gate Status: $qgStatus"

                                            if ($qgStatus -eq 'OK') {
                                                Write-Host 'Quality Gate: PASSED'
                                                exit 0
                                            } else {
                                                Write-Host 'Quality Gate: FAILED'
                                                exit 1
                                            }
                                        } catch {
                                            Write-Host "Error checking Quality Gate: $_"
                                            exit 2
                                        }
                                        "
                                    ''',
                                    returnStatus: true
                                )

                                if (statusCode == 0) {
                                    echo '✓ Quality Gate PASSED - Proceeding to Docker build'
                                    return
                                } else if (statusCode == 1) {
                                    error('✗ Quality Gate FAILED - Check SonarQube for violations')
                                } else {
                                    echo 'Retrying Quality Gate check...'
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Build & Push Docker Image') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                echo 'Building Docker image...'
                bat "docker build -t ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:${env.IMAGE_TAG} ."
                bat "docker tag ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:${env.IMAGE_TAG} ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:latest"

                echo 'Pushing to Docker Hub...'
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials',
                                                 usernameVariable: 'DOCKER_USER',
                                                 passwordVariable: 'DOCKER_PASS')]) {

                    bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                    bat "docker push ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                    bat "docker push ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:latest"
                    bat "docker logout"
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution completed.'
            cleanWs()
        }
        success {
            echo 'Build and Push succeeded!'
        }
        failure {
            echo 'Build failed.'
        }
    }
}
