#!/usr/bin/env groovy
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def ws = "${env.WORKSPACE}"
podTemplate(label: label,
        containers: [
                yaml """
                kind: Pod
                metadata:
                  labels:
                    name: "docker"
                spec:
                  containers:
                  - name: jnlp
                    volumeMounts:
                      - name: docker-config-json-volume
                        mountPath: /root/.docker
                  volumes:
                  - name: docker-config-json-volume
                    secret:
                      secretName: 'docker-config-json
                      items:
                      - key: .dockerconfigjson
                        path: config.json
"""
                //containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
                   // envVars: [secretEnvVar(key: 'DOCKER_CONFIG', secretName: 'docker-config-json', secretKey: '.dockerconfigjson'), ])],
                volumes: [
                    //secretVolume(secretName: 'docker-config-json', mountPath: "/root/.docker", items: [key: '.dockerconfigjson', path: 'config.json']),
                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                ],
                imagePullSecrets: [ 'docker-config-json' ]
        ) {
    node(label) {
    /*
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
                           cd helloworld-ws && docker build -t nexus-dock.k8s.playpit.by/vpupkin/app:${env.BUILD_NUMBER} .
                           """
                    }
                }
                */
                stage('Docker Push') {
                    container('docker') {
                        echo "Building docker image..."
                        sh """
                           pwd
                           echo
                           ls -la ~/
                           ls -la ~/.docker
                           ls -la ${env.WORKSPACE}
                           echo
                           env
                           docker login -u admin -p admin nexus-dock.k8s.playpit.by
                           dokcer push nexus-dock.k8s.playpit.by/vpupkin/app:${env.BUILD_NUMBER}
                           """
                    }
                }
    }
}
