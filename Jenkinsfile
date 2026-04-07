pipeline {
    agent any

    tools {
        maven 'Local Maven 3.9.11'
        jdk 'Local JDK-21'
    }

    environment {
        DOCKER_HUB_USER = 'mkiavash'
        IMAGE_NAME = 'shopping-cart-calc'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git rev-parse --abbrev-ref HEAD && git rev-parse --short HEAD'
            }
        }

        stage('Clean & Compile') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('Test + JaCoCo') {
            steps {
                sh 'mvn test verify -Pcoverage'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:${env.IMAGE_TAG} ."
                sh "docker tag ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:${env.IMAGE_TAG} ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:latest"
            }
        }

        stage('Push Docker Image') {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    sh "docker push ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                    sh "docker push ${env.DOCKER_HUB_USER}/${env.IMAGE_NAME}:latest"
                    sh 'docker logout'
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}