#!/usr/bin/env groovy

node {
    stage('01 git checkout') {
//‘Preparation (Checking out)’
        //git checkout scm
        checkout scm
        stash 'source'
//            git branch: 'master', credentialsId: '12345-1234-4696-af25-123455',
//                url: 'ssh://git@bitbucket.org:company/repo.git'
      }
  }
  stage('02 Building code') {
      unstash 'source'
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


