#!/usr/bin/env groovy

node {
  stage('01 git checkout') {
    checkout([$class: 'GitSCM',
      branches: [[name: 'origin/ibletsko']],
      userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]
    ])
    stash 'source'
  }

  stage('02 Building code') {
      unstash 'source'
      mvn -f helloworld-project/helloworld-ws/pom.xml package
  }

  stage('Sonar scan') {
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
