#!/usr/bin/env groovy

node {
    stage('checking_out') {
        git([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
    }
    stage ('building_code') {
        withMaven(maven: 'maven-3') {
        sh 'mvn clean install -f helloworld-project/helloworld-ws/pom.xml'
    }
}
}
