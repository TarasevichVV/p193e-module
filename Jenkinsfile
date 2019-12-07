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
    def scannerHome = tool 'Sonar';
    withSonarQubeEnv('Sonar') {
      sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=sonarcheck -Dsonar.sources=helloworld-project/helloworld-ws/src -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
    }
  }

  stage('Testing’') {
    parallel (
      "Task1" : {
        sh 'echo "mvn pre-integration-test"'
      },
      "Task2" : {
        withMaven(maven: 'M3') {
          "sh 'mvn integration-test'"
        }
      },
      "Task3" : {
        sh 'echo "mvn post-integration-test"'
      }
    )
  }

  stage('Triggering job and fetching artefact') {
    build job: 'MNTLAB-ibletsko-child1-build-job', parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: 'ibletsko']]

//after finishing
// fetch bletsko_dsl_script.tar.gz
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
