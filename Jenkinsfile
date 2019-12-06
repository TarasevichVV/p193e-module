#!/usr/bin/env groovy
def label = "docker-jenkins-${UUID.randomUUID().toString()}"

podTemplate(label: label,
        containers: [
                containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
            ],
            volumes: [
                hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                hostPathVolume(hostPath: "${env.WORKSPACE}", mountPath: "${env.WORKSPACE}"),
            ]
        ) {
    node(label) {
                stage ('Checkout&Build'){
                    checkout scm
                    sh """
                        echo "${env.WORKSPACE}"
                        ls -la
                        pwd
                        """
                    withMaven(maven: 'M3') {
                       sh "cd helloworld-ws && mvn clean install"
                    }
                }

                stage('Docker Build') {
                    container('docker') {
                        echo "Building docker image..."
                        sh """
                           pwd
                           ls -la
                           cd helloworld-ws && docker build -t nexus-dock.k8s.playpit.by:80/vpupkin/app:${env.BUILD_NUMBER} .
                           """
                    }
                }
                stage('Docker Push') {
                    container('docker') {
                        echo "Building docker image..."
                        sh """
                           docker ps
                           docker images
                           docker login -u admin -p admin nexus-dock.k8s.playpit.by
                           dokcer push nexus-dock.k8s.playpit.by:80/vpupkin/app:${env.BUILD_NUMBER}
                           """
                    }
                }                
    }
}
