pipeline {
    agent any

    environment {
        IMAGE_TAG = "${BUILD_NUMBER}"
        DOCKER_HUB_USER = 'xxxxxxxxxx'
        GIT_CREDENTIALS = 'xxxxxxxxxxxx'
        GIT_REPO = 'https://github.com/xxxxxxxxxx/github-project-name'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git credentialsId: "${GIT_CREDENTIALS}", url: "${GIT_REPO}", branch: 'main'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = [
                        'authentication-service',
                        'wallet-service',
                        'history-service',
                        'withdrawal-service',
                        'deposit-service',
                        'maintenance-service',
                        'blacklist-service',
                        'bank-collection-service',
                        'revenue-service',
                        'beneficiary-service'
                    ]
                    services.each { service ->
                        sh """
                        echo "Building Docker Image for ${service}"
                        docker build -t ${DOCKER_HUB_USER}/${service}:${IMAGE_TAG} ./${service}
                        """
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    sh 'echo $DOCKER_HUB_PASSWORD | docker login -u $DOCKER_HUB_USER --password-stdin'
                    def services = [
                        'authentication-service',
                        'wallet-service',
                        'history-service',
                        'withdrawal-service',
                        'deposit-service',
                        'maintenance-service',
                        'blacklist-service',
                        'bank-collection-service',
                        'revenue-service',
                        'beneficiary-service'
                    ]
                    services.each { service ->
                        sh "docker push ${DOCKER_HUB_USER}/${service}:${IMAGE_TAG}"
                    }
                }
            }
        }

        stage('Deploy Microservices') {
            steps {
                script {
                    sh '''
                    echo "Restarting Microservices with New Images"
                    docker-compose down
                    docker-compose up -d --build
                    '''
                }
            }
        }
    }
}
