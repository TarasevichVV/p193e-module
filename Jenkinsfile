#!/usr/bin/env groovy
student='melizarov'
mails_to_notify='mk-ez@mmmk.com'
node {
    try {
        stage('1-Checkout') {
            checkout scm
            echo "checkout from dev branch"
        }

        stage('2-Build') {
            git branch: "${student}", url: 'https://github.com/MNT-Lab/build-t00ls.git'
            withMaven(maven: "M3") { //maven3-6-3
                sh "mvn -f helloworld-project/helloworld-ws/pom.xml package"
            }
        }
        stage('3-Sonar') {
            //   environment {
            def scannerHome = tool 'Sonar'
            //  }
            withSonarQubeEnv('Sonar') {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${student} -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }
            /*        timeout(time: 10, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true

            }*/
        }
        stage('4-Test') {
            parallel(
                    preintegration: {
                        sh 'echo \'mvn pre-integration-test\''
                    },
                    integrationtest: {
                        sh 'echo \'This is integration-test\''
                        sh 'mvn integration-test -f helloworld-ws/pom.xml'
                    },
                    postintegration: {
                        sh "echo \'mvn post-integration-test\'"
                    }
            )
        }
        stage('5-Triggering') {

            sh "echo trigering"
        }
        stage('6-Packeging') {

            sh "echo Packeging"

        }
        stage('7-Asking approval') {

            sh "echo Asking"

        }
        stage('8-Deploy') {

            sh "echo deploy"

        }
        stage('9-Sending status') {

            sh "echo Sending status"

        }
    } catch (e) {
        String error = "${e}";
        // Make the string with job info, example:
        // ${env.JOB_NAME}
        // ${env.BUILD_NUMBER}
        // ${env.BUILD_URL}
        // and other variables in the code
        mail bcc: '',
                cc: '',
                charset: 'UTF-8',
                from: '',
                mimeType: 'text/html',
                replyTo: '',
                subject: "ERROR CI: Project name -> ${env.JOB_NAME}",
                to: "${mails_to_notify}",
                body: "<b>${pivote}</b><br>\n\nError mesage: ${error}\n\n<br>Project: ${env.JOB_NAME} <br>Build Number: ${env.BUILD_NUMBER} <br> URL de build: ${env.BUILD_URL}";
        error "${error}"
    }
}