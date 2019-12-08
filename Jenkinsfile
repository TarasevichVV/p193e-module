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
/*        stage('3-Sonar') {
            //   environment {
            def scannerHome = tool 'Sonar'
            //  }
            withSonarQubeEnv('Sonar') {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${student} -e       -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }
            *//*        timeout(time: 10, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true

            }*//*
        }*/
/*        stage('4-Tests') {
            parallel(
                    '4-1-preintegration': {
                        sh 'echo \'mvn pre-integration-test\''
                    },
                    '4-2-integrationtest': {
                        sh 'echo \'This is integration-test\''
                        withMaven(maven: "M3") {
                            sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml'
                        }
                    },
                    '4-3-postintegration': {
                        sh 'echo \'mvn post-integration-test\''
                    }
            )
        }*/
        stage('5-Triggering') {
            build job: 'MNTLAB-${student}-child1-build-job', parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]], wait: true
            copyArtifacts fingerprintArtifacts: true, projectName: 'MNTLAB-${student}-child1-build-job', selector: lastSuccessful()
            sh 'find / -name output.txt'
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
                //body: "<b>${pivote}</b><br>\n\nError mesage: ${error}\n\n<br>Project: ${env.JOB_NAME} <br>Build Number: ${env.BUILD_NUMBER} <br> URL de build: ${env.BUILD_URL}";
                body: "<br>\n\nError mesage: ${error}\n\n<br>Project: ${JOB_NAME} <br>Build Number: ${BUILD_NUMBER} <br> URL de build: ${BUILD_URL}";
        error "${error}"
    }
}