#!/usr/bin/env groovy
def label = "docker-jenkins-${UUID.randomUUID().toString()}"

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
            stage('Sab') {
                container('docker') {
                    echo "Building docker image..."
                    sh """
                      hostname
                      whoami
                      env
                      echo $PATH
                      ps -ef 
                      docker version
                      """
                }
            }
    }
}
