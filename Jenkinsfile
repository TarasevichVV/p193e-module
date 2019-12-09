#!/usr/bin/env groovy
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def student = "dprusevich"

node {

  stage ('Check out scm') {
    checkout scm
  }
  stage('Build') {
    git branch: 'dprusevich', url: 'https://github.com/MNT-Lab/build-t00ls'
    sh '''
    build_time="$(echo $(date +'%Y%m%d_%H:%M:%S'))"
    version="$BUILD_NUMBER"
    triggered_by="$(echo $(git --no-pager show -s --format='%an' $GIT_COMMIT))"
    sed -i "s|<body>|&\\nversion=\"${version}\"  \\nBuildTime=\"${build_time}\" \\nTriggeredBy=\"$triggered_by\" |" helloworld-project/helloworld-ws/src/main/webapp/index.html
    '''
    withMaven(maven: "M3") {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean package"
    }
  }
}


