#!/usr/bin/env groovy

node {
  stage('01 git checkout') {
    checkout([$class: 'GitSCM',
      branches: [[name: 'origin/ibletsko']],
      userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]
    ])
    stash 'source'
//‘Preparation (Checking out)’
      //git checkout scm
//    checkout scm
//      checkout([$class: 'GitSCM',
//      branches: [
//        [name: 'env.GIT_BRANCH/${branchToBuild}']
//      ],
/*     checkout([
        $class: 'GitSCM',
        branches: scm.branches,
        extensions: scm.extensions + [[$class: 'LocalBranch'], [$class: 'WipeWorkspace']],
        userRemoteConfigs: [[credentialsId: 'Bitbucket', url: 'git@bitbucket.org:NAVFREG/jenkinsfile-tests.git']],
        doGenerateSubmoduleConfigurations: false
    ]) */
//            git branch: 'master', credentialsId: '12345-1234-4696-af25-123455',
//                url: 'ssh://git@bitbucket.org:company/repo.git'
  }

  stage('02 Building code') {
      unstash 'source'
// https://github.com/MNT-Lab/build-t00ls.git
// mvn package – package the src
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
