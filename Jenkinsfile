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
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
      sh "cp helloworld-project/helloworld-ws/target/*.war ." 
    }
  }
/*
  stage ('Sonar scan') {
    def scannerHome = tool 'Sonar'
    withSonarQubeEnv('Sonar') {
    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${student}_helloworld -Dsonar.sources=helloworld-project/helloworld-ws/src -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
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
*/
  stage ('Triggering job and fetching artefact after finishing') {
    build job: "MNTLAB-${student}-child1-build-job",
    parameters: [ string(name: "BRANCH_NAME", value: "${student}") ], wait: true
    copyArtifacts (projectName: "MNTLAB-${student}-child1-build-job", selector: lastSuccessful())
    }
    sh "ls"

  stage ('Packaging and Publishing results') {
  
    git branch: "${student}", url: 'https://github.com/MNT-Lab/p193e-module.git'
    sh """
    tar -zxvf "${student}"_dsl_script.tar.gz output.txt
    cp "${JENKINS_HOME}"/workspace/EPBYMINW8538/mntlab-ci-pipeline@script/Jenkinsfile .
    tar czf pipeline-"${student}"-"${BUILD_NUMBER}".tar.gz output.txt Jenkinsfile helloworld-ws.war
    """
    sh """
    cat << "EOF" > Dockerfile
FROM tomcat:8.0
MAINTAINER Dzmitry Prusevich
COPY helloworld-ws.war /usr/local/tomcat/webapps/
    """
    sh """
    curl -v -u admin:admin --upload-file pipeline-"${student}"-"${BUILD_NUMBER}".tar.gz \
    nexus.k8s.playpit.by/repository/maven-releases/app/"${student}"/"${BUILD_NUMBER}"/pipeline-"${student}"-"${BUILD_NUMBER}".tar.gz
    """
  }

  stage ('Docker deploy') {
  podTemplate(label: label,
    containers: [
        containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
        containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
      ],
      volumes: [
        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
      ]
    ) {
    node(label) {
        container('docker') {
        echo "Building docker image..."
        sh """
cat << "EOF" > Dockerfile
FROM tomcat:8.0
MAINTAINER Dzmitry Prusevich
COPY helloworld-ws.war /usr/local/tomcat/webapps/
"""
        sh """
          student="dprusevich"
          docker build -t helloworld-"{student}":"${BUILD_NUMBER}" .
          docker tag helloworld-"{student}":"${BUILD_NUMBER}" nexus-dock.k8s.playpit.by:80/helloworld-"{student}":"${BUILD_NUMBER}"
          docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
          docker push nexus-dock.k8s.playpit.by:80/helloworld-"{student}":"${BUILD_NUMBER}"
          docker rmi helloworld-"{student}":"${BUILD_NUMBER}"
        """
        }
    }
}
}
}








