#!/usr/bin/env groovy
def label = "docker-jenkins-${UUID.randomUUID().toString()}"

podTemplate(label: label,
            yaml: """
                kind: Pod
                metadata:
                  labels:
                    name: "docker"
                spec:
                  containers:
                  - name: jnlp
                    workingDir: /home/jenkins/agent
                    image: 'jenkins/jnlp-slave:alpine'
                  - name: docker
                    image: docker
                    command:
                      - cat
                    tty: true
                    volumeMounts:
#                      - name: docker-config-json-volume
#                        mountPath: /root/.docker
                      - name: docker-sock-volume
                        mountPath: /var/run/docker.sock
                  volumes:
#                  - name: docker-config-json-volume
#                    secret:
#                      secretName: docker-config-json
#                      items:
#                      - key: .dockerconfigjson
#                        path: config.json
                  - name: docker-sock-volume
                    hostPath:
                        path: /var/run/docker.sock
                        type: File


"""){

/*
podTemplate(label: label,
        containers: [
                //containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
                   // envVars: [secretEnvVar(key: 'DOCKER_CONFIG', secretName: 'docker-config-json', secretKey: '.dockerconfigjson'), ])],
                volumes: [
                    //secretVolume(secretName: 'docker-config-json', mountPath: "/root/.docker", items: [key: '.dockerconfigjson', path: 'config.json']),
                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                ],
                imagePullSecrets: [ 'docker-config-json' ]
        ) {
*/
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
                           id

                           pwd
                           env
                           docker login -u admin -p admin http://nexus-dock.k8s.playpit.by:80
                           docker push nexus-dock.k8s.playpit.by:80/vpupkin/app:${env.BUILD_NUMBER}
                           """
                    }
                }

                stage('Docker Pull') {
                    container('docker') {
                        echo "Pull docker image..."
                        sh """
                           docker rmi nexus-dock.k8s.playpit.by:80/vpupkin/app:${env.BUILD_NUMBER}
                           docker pull nexus-dock.k8s.playpit.by:80/vpupkin/app:${env.BUILD_NUMBER}
                           """
                    }
                }


    }
}
