#!/usr/bin/env groovy
def student = "ibletsko"
def job_to_use = "MNTLAB-ibletsko-child1-build-job"

node {
  stage('01 git checkout') {
    checkout scm
  }

  stage('02 Building code') {
    checkout([$class: 'GitSCM',
      branches: [[name: "origin/ibletsko"]],
      userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]
    ])
    withMaven(maven: 'M3') {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml package"
    }
    sh "ls -la"
    sh "ls helloworld-project/helloworld-ws/"
  }

  stage('03 Sonar scan') {
/*     def scannerHome = tool 'Sonar';
    withSonarQubeEnv('Sonar') {
      sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=sonarcheck -Dsonar.sources=helloworld-project/helloworld-ws/src -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
    }
 */  }

  stage('04 Testing') {
    parallel (
      "parallel 1" : {
        sh 'echo "PARALLEL 1: mvn pre-integration-test"'
      },
      "parallel 2" : {
        withMaven(maven: 'M3') {
//          sh 'echo "PARALLEL 2: mvn integration-test"'
          "sh 'mvn integration-test'"
        }
      },
      "parallel 3" : {
        sh 'echo "PARALLEL 3: mvn post-integration-test"'
      }
    )
  }

  stage('05 Triggering job and fetching artefact') {
    build job: "${job_to_use}", parameters: [
      [$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]//, wait: true by default
    ]
    copyArtifacts projectName: "${job_to_use}", selector: lastCompleted()
    archiveArtifacts '*'
  }

  stage('06 Packaging and Publishing results') {
// Jenkinsfile
    sh "tar -czf pipeline-${student}-${BUILD_NUMBER}.tar.gz helloworld-ws.war output.tx"
    sh "ls -la"
    //create docker image 'helloworld-{student}:{buildNumber}'
    //push archive to nexus
    
  //  sh 'make'
  //  archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
  }

  stage('07 Asking for manual approval') {
  }

  stage('08 Deployment') {
//(rolling update, zero downtime)

  }
}
