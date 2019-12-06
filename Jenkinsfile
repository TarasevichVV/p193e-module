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
            dir('helloworld-ws'){
                stage ('Checkout&Build'){
                    checkout scm
                    sh '''
                        ls -la
                        pwd
                        '''
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
}
