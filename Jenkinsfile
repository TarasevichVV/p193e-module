#!/usr/bin/env groovy

node {
    stage('01 git checkout') {
      steps {//‘Preparation (Checking out)’
        //git checkout scm
        checkout scm
        stash 'source'
//            git branch: 'master', credentialsId: '12345-1234-4696-af25-123455',
//                url: 'ssh://git@bitbucket.org:company/repo.git'
      }
    }
  }
  stage('02 Building code') {
    steps {
      unstash 'source'

    }
  }
  stage('Sonar scan') {
    steps {
    }
  }
  stage('Testing’') {
    steps {
    }
  }
  stage('Triggering job and fetching artefact') {//after finishing
    steps {
    }
  }
  stage('Packaging and Publishing results') {
    steps {
//  sh 'make'
//  archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
    }
  }
  stage('Asking for manual approval') {
    steps {
    }
  }
  stage('Deployment') {//(rolling update, zero downtime)
    steps {
    }

}


