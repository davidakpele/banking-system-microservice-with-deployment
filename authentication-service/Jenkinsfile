pipeline {
    agent any
    parameters {
        string(name: 'REPO_NAME', defaultValue: 'authentication-service', description: 'Name of the repository')
        string(name: 'DOCKER_IMAGE', defaultValue: 'xxxxxx/authentication-service:latest', description: 'Docker image name')
    }
    environment {
        DOCKER_CREDENTIALS_ID = credentials('docker-credentials-id')  // Secure credentials storage
        K8S_CREDENTIALS_ID = credentials('k8s-oidc-token')  // Using OIDC authentication
        K8S_SERVER_URL = 'https://your-k8s-server-url'
        K8S_NAMESPACE = 'webapps'
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    echo "Starting pipeline for ${params.REPO_NAME}"
                }
            }
        }

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout Repository') {
            steps {
                git url: 'https://your-repo-url.git', branch: 'main', credentialsId: 'git-credentials-id'
            }
        }

        stage('Build & Test') {
            environment {
                BUILD_ENV = 'production'
            }
            steps {
                sh './mvnw clean package -DskipTests'
                sh './mvnw test'
            }
        }

        stage('Build Secure Docker Image') {
            steps {
                script {
                    docker.build("${env.DOCKER_IMAGE}:${env.BUILD_NUMBER}")
                }
            }
        }

        stage('Push Secure Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDENTIALS_ID) {
                        docker.image("${env.DOCKER_IMAGE}:${env.BUILD_NUMBER}").push()
                    }
                }
            }
        }

        stage('Deploy to Secure Kubernetes') {
            steps {
                withKubeCredentials(kubectlCredentials: [[
                    clusterName: 'EKS-1',
                    credentialsId: 'k8s-oidc-token',  // OIDC authentication
                    namespace: 'webapps',
                    serverUrl: K8S_SERVER_URL
                ]]) {
                    sh "kubectl apply -f deployment-service.yml"
                    sh "kubectl set image deployment/authentication-service authentication-service=${env.DOCKER_IMAGE}:${env.BUILD_NUMBER}"
                    sleep 60
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                withKubeCredentials(kubectlCredentials: [[
                    clusterName: 'EKS-1',
                    credentialsId: 'k8s-oidc-token',
                    namespace: 'webapps',
                    serverUrl: K8S_SERVER_URL
                ]]) {
                    sh "kubectl get svc -n webapps"
                    sleep 30
                }
            }
        }

        stage('Deploy ELK for Security Monitoring') {
            steps {
                withKubeCredentials(kubectlCredentials: [[
                    clusterName: 'EKS-1',
                    credentialsId: 'elk-k8-token',
                    namespace: 'elk',
                    serverUrl: 'xxxxxxxxx'
                ]]) {
                    sh "kubectl apply -f ./elk"
                    sleep 30
                }
            }
        }

        stage('Verify ELK Deployment') {
            steps {
                withKubeCredentials(kubectlCredentials: [[
                    clusterName: 'EKS-1',
                    credentialsId: 'elk-k8-token',
                    namespace: 'elk',
                    serverUrl: 'xxxxxxxxx'
                ]]) {
                    sh "kubectl get svc -n elk"
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment succeeded!'
            sh "kubectl logs deployment/authentication-service -n webapps | tail -n 20"  // Fetch logs
        }
        failure {
            echo 'Deployment failed!'
            sh "kubectl logs deployment/authentication-service -n webapps | tail -n 50"  // Fetch last 50 logs for debugging
        }
        always {
            cleanWs()
        }
    }
}
