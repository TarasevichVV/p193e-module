#!/usr/bin/env groovy
def student = "ashvedau"
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def label2 = "centos-jenkins-${UUID.randomUUID().toString()}"
def nexus = "nexus-dock.k8s.playpit.by:80"

node {
    try {
        stage("1. Preparation (checking out)"){
            echo "====++++executing Preparation (checking out)++++===="
            checkout scm
        }

        stage('2. Build') {
            echo "====++++executing sonar scan++++===="
            git branch: "ashvedau", url: "https://github.com/MNT-Lab/build-t00ls"
            def index = ''' 
                <p> AUTHOR = ashvedau </p>
                <p> JOB_NAME = "$JOB_NAME" </p>
                <p> COMMIT_ID = "$GIT_COMMIT" </p>
                <p> BUILD_TIME = "$(date)" </p>
                <p> ARTIFACT_VERSION = 1.0."$BUILD_NUMBER" </p>
            '''
            sh 'echo "${index}" > helloworld-project/helloworld-ws/src/main/webapp/index.html'

            withMaven(maven: "M3") {
                sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
            }
        }

        stage("3. Sonar scan"){
            echo "====++++executing sonar scan++++===="
            def scannerHome = tool name: 'Sonar'
            withSonarQubeEnv(installationName: 'Sonar') {
                sh "${scannerHome}/bin/sonar-scanner  -e -Dsonar.projectKey=${student} -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }
        }
    }
    catch (err) {
        currentBuild.result = 'FAILURE'
    }
}