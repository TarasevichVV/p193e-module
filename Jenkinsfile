#!/usr/bin/env groovy
def student = "ibletsko"
def job_to_use = "MNTLAB-ibletsko-child1-build-job"

node {
  stage('01 git checkout') {
    checkout scm
  }

  stage('02 Building code') {
    checkout([$class: 'GitSCM',
      branches: [[name: "origin/${student}"]],
      userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]
    ])
    withMaven(maven: 'M3') {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml package"
    }
//    sh "ls -la"
//    sh "ls helloworld-project/helloworld-ws/"
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
//          "sh 'mvn integration-test'"
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
//    sh "ls helloworld-project/helloworld-ws/target/"
    writeFile file: "Jenkinsfile", text: "For testing purposes."
    sh "tar -czf pipeline-${student}-${BUILD_NUMBER}.tar.gz helloworld-project/helloworld-ws/target/helloworld-ws.war output.txt Jenkinsfile"
    //create docker image 'helloworld-{student}:{buildNumber}'
    sh """
    cat > Dockerfile.1 << EOF
    FROM tomcat:8.0
    COPY helloworld-project/helloworld-ws/target/helloworld-ws.war /usr/local/tomcat/webapps/
    EOF
    """
    wrappers {
        buildInDocker {
          dockerfile {
            filename 'Dockerfile.1'
          }
  //            volume('/dev/urandom', '/dev/random')
              verbose()
          }
    }

    sh "ls -la"
    sh "docker images"
    //push archive to nexus
  }

  stage('07 Asking for manual approval') {
  }

  stage('08 Deployment') {
//(rolling update, zero downtime)

  }
}
