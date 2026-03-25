pipeline {
    agent any

    tools {
        maven 'Local Maven 3.9.11'
        jdk 'Local JDK-21'
        git 'Local git exe'
    }
    environment {
        JAVA_HOME = tool name: 'JDK-21', type: 'jdk'
        MAVEN_HOME = tool name: 'Maven-3.9.11', type: 'maven'
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

                sh 'java -version'
                sh 'mvn -version'

                sh 'mvn clean'

                sh 'mkdir -p target/coverage-reports'
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling source code...'
                sh 'mvn compile -Dmaven.test.skip=true'
            }
            post {
                success {
                    echo 'Compilation successful!'
                }
                failure {
                    echo 'Compilation failed!'
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests with JaCoCo coverage...'
                sh 'mvn test jacoco:report'
            }
            post {
                always {
                    // Publish JUnit test results
                    junit 'target/surefire-reports/*.xml'

                    // Archive test reports
                    archiveArtifacts artifacts: 'target/surefire-reports/**', fingerprint: true

                    // Archive JaCoCo coverage reports
                    archiveArtifacts artifacts: 'target/site/jacoco/**', fingerprint: true
                }
                success {
                    echo 'All tests passed successfully!'
                }
                failure {
                    echo 'Some tests failed. Check test reports for details.'
                }
            }
        }

        stage('Coverage Report') {
            steps {
                echo 'Generating coverage report...'
                jacoco(
                    execPattern: 'target/coverage-reports/jacoco.exec',
                    classPattern: 'target/classes',
                    sourcePattern: 'src/main/java',
                    exclusionPattern: '**/ShoppingCartCalculator.class'
                )

                // recordCoverage metrics
                recordCoverage(
                    tools: [
                        jacoco(
                            execPattern: 'target/coverage-reports/jacoco.exec'
                        )
                    ],
                    sourceDirectories: ['src/main/java']
                )
            }
        }

        stage('Check Coverage Threshold') {
            steps {
                echo 'Checking coverage thresholds...'
                sh 'mvn jacoco:check'
            }
            post {
                success {
                    echo 'Coverage meets minimum thresholds!'
                }
                failure {
                    echo 'Coverage is below minimum thresholds!'
                    // Fail the build if coverage is too low
                    error 'Coverage threshold not met'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application...'
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    // Archive the built JAR file
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    echo 'Package created successfully!'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution completed.'

            // Cleans the workspace up
            cleanWs()
        }
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'

        }
        unstable {
            echo 'Build is unstable!'
        }
        aborted {
            echo 'Build was aborted!'
        }
    }
}