#!/usr/bin/env groovy
def student = "ibletsko"
def job_to_use = "MNTLAB-ibletsko-child1-build-job"

node {
  stage('01 git checkout') {
    checkout scm
    checkout([$class: 'GitSCM',
      branches: [[name: 'origin/ibletsko']],
      userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/p193e-module.git']]
    ])
    sh "ls -la"
    stash includes: "Jenkinsfile", name: "st_jenkinsfile"
    stash includes: "Dockerfile", name: "st_dockerfile"
    stash includes: "*.yml", name: "st_yamls"

    checkout([$class: 'GitSCM',
      branches: [[name: 'origin/ibletsko']],
      userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]
    ])
    stash 'mnt-source'

    sh "ls -la"
  }

  stage('02 Building code') {
    withMaven(maven: 'M3') {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml package"
    }
  }

  stage('03 Sonar scan') {
// WORKING --
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
// WORKING --
//          "sh 'mvn integration-test'"
        }
      },
      "parallel 3" : {
        sh 'echo "PARALLEL 3: mvn post-integration-test"'
      }
    )
  }

  stage('05 Triggering job and fetching artefact') {
// WORKING --
/*     build job: "${job_to_use}", parameters: [
      [$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]//, wait: true by default
    ]
    copyArtifacts projectName: "${job_to_use}", selector: lastCompleted() */
    //
    writeFile file: "output.txt", text: "output.txt For testing purposes."
    stash includes: "*.txt", name: "st_output"
    archiveArtifacts '*'
  }

  stage('06 Packaging and Publishing results') {
    parallel (
      "parallel 1: archiving" : {
//    writeFile file: "Jenkinsfile", text: "For testing purposes."
        unstash "st_jenkinsfile"
        unstash "st_output"
        sh """
          tar -czf pipeline-${student}-${BUILD_NUMBER}.tar.gz helloworld-project/helloworld-ws/target/helloworld-ws.war output.txt Jenkinsfile
          curl -v -u admin:admin --upload-file pipeline-${student}-${BUILD_NUMBER}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${BUILD_NUMBER}/pipeline-${student}-${BUILD_NUMBER}.tar.gz
          """
      },
      "parallel 2: image" : {
        def nodelabel = "buildnode"
        def nexusaddr = "nexus-dock.k8s.playpit.by:80"
        sh "echo parallel 2: image"
        podTemplate (label: nodelabel, containers: [
          containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true)
          ],
          volumes: [
            hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
            ]) {
            node(nodelabel) {
              stage('build image') {
                container('docker') {
                  unstash "st_jdockerfile"
                  sh """
                    docker build -t $nexusaddr/helloworld-$student:$BUILD_NUMBER .
                    docker login -u admin -p admin $nexusaddr
                    docker push $nexusaddr/helloworld-$student:$BUILD_NUMBER
                    """
                }
              }
            }
          } 
      }
    )
    stash includes: "helloworld-project/helloworld-ws/target/helloworld-ws.war", name: "file2"
  }

  stage('07 Asking for manual approval') {
// WORKING --
/*     script {
      timeout(time: 5, unit: 'MINUTES') {
        input(id: "Deploy Gate", message: "Deploy ${currentBuild.projectName}?", ok: 'Deploy')
      }
    } */
  }

  stage('08 Deployment') {
    podTemplate (label: 'testnode', containers: [
        containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
        containerTemplate(name: 'centos', image: 'centos', ttyEnabled: true)
    ]) {
      node('testnode') {
        container('centos') {
          unstash "st_yamls"
          sh """
          curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
          chmod +x ./kubectl
          mv ./kubectl /usr/local/bin/kubectl
          sed -i "s/_deploy_ver_/$BUILD_NUMBER/" deploy-all.yml
          kubectl apply -f deploy-all.yml
          kubectl get pod -A
          """
        }
      }
    }
  }
}




