#!/usr/bin/env groovy

node {
    stage ('checking_out') {
        git([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
    }
    stage ('building_code') {
        git ([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
        withMaven(maven: 'Maven-1') {
            sh 'mvn clean install -f helloworld-project/helloworld-ws/pom.xml'
    }
    }
    stage ('sonar_scan') {
    def scannerHome = tool 'Sonar-scanner'
    withSonarQubeEnv() {"""
        sh "${scannerHome}/bin/sonar-scanner" 
        -e -Dsonar.host.url=http://sonar.default.svc.cluster.local/sonar
        -e -Dsonar.projectKey=number${BUILD_NUMBER}
        -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target 
        -e -Dsonar.sources=helloworld-project/helloworld-ws/src 
        """}
    }
}
