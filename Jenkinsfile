//#parse("File Header.java")
node {
    stage('Checkout') {
        steps {
            scm {
                git {
                    remote {
                        url('https://github.com/MNT-Lab/build-t00ls.git')
                    }
                    branch('melizarov')
                }
                echo "checkout from dev branch"
            }
        }
        stage('Build') {
            steps {
                withMaven(maven: "M3") { //maven3-6-3
                    sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
                }
            }
        stage('Sonar'){
            environment {
                scannerHome = tool 'SonarQubeScanner'
            }
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh "${scannerHome}/bin/sonar-scanner"
                }
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Test') {
            steps {
                parallel(
                    Pre-integration: {
                        sh "echo 'mvn pre-integration-test'"
                    },
                    Integration-test: {
                        echo 'This is integration-test'
                        sh 'mvn integration-test -f helloworld-ws/pom.xml'
                    },
                    Post-integration: {
                        sh "echo 'mvn post-integration-test'"
                    }
                )
            }

        }
        stage('Triggering') {
            steps {
                sh "echo trigering"
            }
        }
        stage('Packeging') {
            steps {
                sh "echo Packeging"
            }
        }
        stage('Asking approval') {
            steps {
                sh "echo Asking"
            }
        }
        stage('Deploy') {
            steps {
                sh "echo deploy"
            }
        }
        stage('Sending status') {
            steps {
                sh "echo Sending status"
            }
        }
    }
}


