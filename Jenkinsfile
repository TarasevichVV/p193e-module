#!/usr/bin/env groovy
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def student = "dprusevich"
node {

  stage ('Preparation (Checking out)') {
    checkout scm
  }
  
  stage ('Building code') {
    git branch: "${student}", url: 'https://github.com/MNT-Lab/build-t00ls'
    sh '''
    build_time="$(echo $(date +'%Y%m%d_%H:%M:%S'))"
    version="$BUILD_NUMBER"
    triggered_by="$(echo $(git --no-pager show -s --format='%an' $GIT_COMMIT))"
    sed -i "s|<body>|&\\nversion=\"${version}\"  \\nBuildTime=\"${build_time}\" \\nTriggeredBy=\"$triggered_by\" |" helloworld-project/helloworld-ws/src/main/webapp/index.html
    cat helloworld-project/helloworld-ws/src/main/webapp/index.html
    '''
    withMaven(maven: "M3") {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean package"
    }
  }

  stage ('Testing') {
    parallel(
      'mvn pre-integration-test': {
        echo "mvn pre-integration-test"
      },
      'mvn integration-test': {
        withMaven(maven: 'M3') {
          sh "mvn -f helloworld-project/helloworld-ws/pom.xml integration-test"
        }
      },
      'mvn post-integration-test': {
        echo "mvn post-integration-test"
      }
    )
  }

  stage ('Triggering job and fetching artefact after finishing') {
    build job: "MNTLAB-${student}-child1-build-job",
    parameters: [ string(name: "BRANCH_NAME", value: "${student}") ], wait: true
    copyArtifacts (projectName: "MNTLAB-${student}-child1-build-job", selector: lastSuccessful())
    }
    sh "ls"
  }






