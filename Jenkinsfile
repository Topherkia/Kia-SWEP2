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
                checkout scm [cite: 2]

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

                bat 'mkdir -p target/coverage-reports'
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling source code...'
                bat 'mvn compile -Dmaven.test.skip=true' [cite: 4]
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
                bat 'mvn test jacoco:report' [cite: 9]
            }
            post {
                always {
                    // Publish JUnit test results
                    junit 'target/surefire-reports/*.xml' [cite: 10]

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
                            execPattern: 'target/jacoco.exec',
                            classPattern: 'target/classes',
                            sourcePattern: 'src/main/java',
                            exclusionPattern: '**/ShoppingCartCalculator.class'
                        )

                        recordCoverage(
                            tools: [[
                                jacoco(path: 'target/site/jacoco/jacoco.xml')
                            ]],
                            sourceDirectories: ['src/main/java']
                        )
                    }
                }

        stage('Check Coverage Threshold') {
            steps {
                echo 'Checking coverage thresholds...'
                bat 'mvn jacoco:check'
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
                bat 'mvn package -DskipTests' [cite: 21]
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
            cleanWs() [cite: 24]
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