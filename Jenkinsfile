#!/usr/bin/env groovy

node {
    stage('checking_out') {
        git([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
    }
    stage ('building_code') {
        git ([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
        withMaven(maven: 'Maven-1') {
            sh 'mvn clean install -f helloworld-project/helloworld-ws/pom.xml'
    }
    }
    stage('sonar_scan') {
    def scannerHome = tool 'Sonar-scanner'
        sh "${scannerHome}/bin/sonar-scanner"
    }
}
