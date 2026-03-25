pipeline {
    agent any

    tools {
        maven 'Local Maven 3.9.11'
        jdk 'Local JDK-21'
    }

    environment {
        JAVA_HOME = tool name: 'Local JDK-21', type: 'jdk'
        MAVEN_HOME = tool name: 'Local Maven 3.9.11', type: 'maven'
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
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/surefire-reports/**', fingerprint: true
                    archiveArtifacts artifacts: 'target/site/jacoco/**', fingerprint: true
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
        stage('Build & Push Docker Image') {
                    when {
                        expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
                    }
                    steps {
                        echo 'Building Docker image...'
                        bat "docker build -t ${DOCKER_HUB_USER}/${IMAGE_NAME}:${IMAGE_TAG} ."
                        bat "docker tag ${DOCKER_HUB_USER}/${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_HUB_USER}/${IMAGE_NAME}:latest"

                        echo 'Pushing to Docker Hub...'
                        // 'docker-hub-credentials' is the ID you created in Jenkins Credentials
                        withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials',
                                                         usernameVariable: 'DOCKER_USER',
                                                         passwordVariable: 'DOCKER_PASS')]) {

                            bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                            bat "docker push ${DOCKER_HUB_USER}/${IMAGE_NAME}:${IMAGE_TAG}"
                            bat "docker push ${DOCKER_HUB_USER}/${IMAGE_NAME}:latest"
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
    }
}