#!/usr/bin/env groovy

node {
    stage ('checking_out') {
        git([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
    }
    stage ('building_code') {
        git ([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
        sh '''
        cat << EOF >> helloworld-project/helloworld-ws/src/main/webapp/index.html
        <p> AUTHOR = phardzeyeu </p>
        <p> JOB_NAME = "$JOB_NAME" </p>
        <p> COMMIT_ID = "$GIT_COMMIT" </p>
        <p> BUILD_TIME = "$(date)" </p>
        <p> ARTIFACT_VERSION = 1.0."$BUILD_NUMBER" </p>
        EOF
        '''
        withMaven(maven: 'Maven-1') {
            sh 'mvn clean install -f helloworld-project/helloworld-ws/pom.xml'
    }
    }
    stage ('sonar_scan') {
    def scannerHome = tool 'Sonar-scanner'
    withSonarQubeEnv() {
        sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=phardzeyeu -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target -e -Dsonar.sources=helloworld-project/helloworld-ws/src" 
        }
    }
    stage ('testing') {
        parallel (
                'pre_integration_test' : { 
                    sh "echo 'mvn pre-integration-test'"
                },
                'integration_test' : {
                    withMaven(maven: 'Maven-1') {
                        sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml'
                        }
                    },
                'post_integration_test' : { 
                    sh "echo 'mvn post-integration-test'"
                }
            )
        }
    stage ('triggering_job') {
        build job: 'MNTLAB-phardzeyeu-child1-build-job', parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: 'phardzeyeu']]
        }
}
