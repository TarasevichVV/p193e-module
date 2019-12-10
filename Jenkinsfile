#!/usr/bin/env groovy
def student = "ibletsko"
def job_to_use = "MNTLAB-${student}-child1-build-job"
def branch = "origin/${student}"
def app_path = "helloworld-project/helloworld-ws"
def app_file = "helloworld-ws.war"
def nexus_dockaddr = "nexus-dock.k8s.playpit.by"
def archive_name = "pipeline-${student}-${BUILD_NUMBER}.tar.gz"

podTemplate (label: 'deploynode', containers: [
  containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
  containerTemplate(name: 'kubework', image: 'cosmintitei/bash-curl', ttyEnabled: true)
])
{
node {
  stage('01 git checkout') {
// workspace cleanup
//    sh "rm -rf *"
    checkout scm
    catchError {
      checkout([$class: 'GitSCM',
        branches: [[name: "origin/${student}"]],
        userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/p193e-module.git']]
      ])
    }
    step([$class: 'Mailer', recipients: 'alert@no.email'])

    stash includes: "Jenkinsfile", name: "st_jenkinsfile"
    stash includes: "Dockerfile", name: "st_dockerfile"
    stash includes: "*.yml", name: "st_yamls"

    checkout([$class: 'GitSCM',
      branches: [[name: "origin/${student}"]],
      userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]
    ])
    stash 'mnt-source'
  }

  stage('02 Building code') {
    catchError {
      withMaven(maven: 'M3') {
        sh """
          echo '<p>buildtime: $(date +'%Y-%m-%d_%H-%M-%S')</p>' > $app_path/src/main/webapp/index.html
          echo '<p>version: $BUILD_NUMBER</p>' > $app_path/src/main/webapp/index.html
        """
        sh "mvn -f ${app_path}/pom.xml package"
      }
    }
    step([$class: 'Mailer', recipients: 'alert@no.email'])
    stash includes: "${app_path}/target/${app_file}", name: "st_warfile"
  }

//commented because sonar pod constantly unavailable due to node resources shortage
/*   stage('03 Sonar scan') {
    def scannerHome = tool 'Sonar';
    catchError {
      withSonarQubeEnv('Sonar') {
        sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=sonarcheck -Dsonar.sources=${app_path}/src -Dsonar.java.binaries=${app_path}/target"
      }
    }
    step([$class: 'Mailer', recipients: 'alert@no.email'])
  } */

  stage('04 Testing') {
    catchError {
      parallel (
        "parallel 1" : {
          sh 'echo "PARALLEL 1: mvn pre-integration-test"'
        },
        "parallel 2" : {
          withMaven(maven: 'M3') {
            "sh 'mvn integration-test -f ${app_path}/pom.xmlintegration-test'"
          }
        },
        "parallel 3" : {
          sh 'echo "PARALLEL 3: mvn post-integration-test"'
        }
      )
    }
    step([$class: 'Mailer', recipients: 'alert@no.email'])
  }

  stage('05 Triggering job and fetching artefact') {
    catchError {
      build job: "${job_to_use}", parameters: [
        [$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]// wait: true by default
      ], wait: true
    }
    step([$class: 'Mailer', recipients: 'alert@no.email'])
    copyArtifacts projectName: "${job_to_use}", selector: lastCompleted()
    stash includes: "*.txt", name: "st_output"
    archiveArtifacts '*'
  }

  stage('06 Packaging and Publishing results') {
    parallel (
      "parallel 1: archiving" : {
        unstash "st_jenkinsfile"
        unstash "st_output"
        sh """
          tar -czf ${archive_name} ${app_path}/target/${app_file} output.txt Jenkinsfile
          curl -v -u admin:admin --upload-file ${archive_name} nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${BUILD_NUMBER}/${archive_name}
        """
      },
      "parallel 2: image" : {
        def nexusaddr = "nexus-dock.k8s.playpit.by:80"
        sh "echo parallel 2: image"
        podTemplate (label: "buildnode", containers: [
          containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true)
          ],
          volumes: [
            hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
          ]) {
          node(nodelabel) {
            container('docker') {
              unstash "st_dockerfile"
              unstash "st_warfile"
              sh """
                docker build -t $nexusaddr/helloworld-$student:$BUILD_NUMBER .
                docker login -u admin -p admin $nexusaddr
                docker push $nexusaddr/helloworld-$student:$BUILD_NUMBER
              """
            }
          }
          }
      }
    )
  }
/* 
  stage('07 Asking for manual approval') {
    script {
      timeout(time: 5, unit: 'MINUTES') {
        input(id: "Deploy Gate", message: "Deploy ${currentBuild.projectName}?", ok: '08 Deployment')
      }
    }
  }
*/

  stage('08 Deployment') {
      node('deploynode') {
        container('kubework') {
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

