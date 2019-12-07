#!/usr/bin/env groovy

node {
  stage('01 git checkout') {
    checkout scm
  }

  stage('02 Building code') {
    checkout([$class: 'GitSCM',
      branches: [[name: 'origin/ibletsko']],
      userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]
    ])
    withMaven(maven: 'M3') {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml package"
    }
  }

  stage('Sonar scan') {
/*
sonar.projectKey=testsonar
sonar.sources=helloworld-project/helloworld-ws/src
sonar.java.binaries=helloworld-project/helloworld-ws/target
sonar.login=admin
sonar.password=admin */
    def scannerHome = tool 'Sonar';
    withSonarQubeEnv('Sonar') {
      sh "${scannerHome}/bin/sonar-scanner"
    }

/*     withSonarQubeEnv('sonar', credentialsId: 'sonar-token') {
//      sh 'mvn clean package sonar:sonar'
      scannerHome = tool 'Sonar'
      }
      steps {
        withSonarQubeEnv('sonarqube')
        {
          sh "${scannerHome}/bin/sonar-scanner"
        }
*/
  }

  stage('Testing’') {
  }

  stage('Triggering job and fetching artefact') {//after finishing
  }

  stage('Packaging and Publishing results') {
  //  sh 'make'
  //  archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
  }

  stage('Asking for manual approval') {
  }

  stage('Deployment') {//(rolling update, zero downtime)
  }
}
