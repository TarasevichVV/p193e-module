#!/usr/bin/env groovy

def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def Dockerfile='''  
FROM alpine

RUN apk update && apk add wget tar openjdk8 && \
wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.20/bin/apache-tomcat-8.5.20.tar.gz && \
tar -xvf apache-tomcat-8.5.20.tar.gz && \
mkdir /opt/tomcat && \
mv apache-tomcat*/* /opt/tomcat/

COPY helloworld-project/helloworld-ws/target/helloworld-ws.war /opt/tomcat/webapps

EXPOSE 8080
CMD ["/opt/tomcat/bin/catalina.sh", "run"]
'''

node {
    stage ('checking_out') {
        checkout scm
    }
    stage ('building_code') {
        git ([url: 'https://github.com/MNT-Lab/d193l-module.git', branch: 'phardzeyeu'])
        stash includes: "Jenkinsfile", name: "jfile"
        git ([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
        sh '''
        cat <<EOF >> helloworld-project/helloworld-ws/src/main/webapp/index.html
        <p> AUTHOR = phardzeyeu </p>
        <p> JOB_NAME = "$JOB_NAME" </p>
        <p> COMMIT_ID = "$GIT_COMMIT" </p>
        <p> BUILD_TIME = "$(date)" </p>
        <p> ARTIFACT_VERSION = 1.0."$BUILD_NUMBER" </p>
        EOF
        '''
        withMaven(maven: 'M3') {
            sh 'mvn clean install -f helloworld-project/helloworld-ws/pom.xml'
            stash includes: "helloworld-project/helloworld-ws/target/helloworld-ws.war", name: "warka"
    }
    }
    stage ('sonar_scan') {
    def scannerHome = tool 'Sonar'
    withSonarQubeEnv() {
        sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=phardzeyeu -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target -e -Dsonar.sources=helloworld-project/helloworld-ws/src" 
        }
    }
    stage ('testing') {
        parallel (
                'pre_integration_test' : { 
                    sh "echo 'mvn pre-integration-test'"
                },
                'integration_test' : {
                    withMaven(maven: 'M3') {
                        sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml'
                        }
                    },
                'post_integration_test' : { 
                    sh "echo 'mvn post-integration-test'"
                }
            )
        }
    stage ('triggering_job') {
        build job: 'MNTLAB-phardzeyeu-child1-build-job', parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: 'phardzeyeu']], wait: true;
        copyArtifacts(projectName: 'MNTLAB-phardzeyeu-child1-build-job', selector: lastSuccessful())
    }
    stage ('packaging_and_publishing_results'){
        parallel (
                'archiving_artifact' : {
                    unstash "jfile"
                    sh """
                    tar zxvf phardzeyeu_dsl_script.tar.gz
                    cp helloworld-project/helloworld-ws/target/helloworld-ws.war .
                    tar czf pipeline-phardzeyeu-${BUILD_NUMBER}.tar.gz output.txt helloworld-ws.war Jenkinsfile
                    """
                },
                'creating_docker_image' : {
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
                            stage('docker_build') {
                                container('docker') {
                                    unstash "warka"
                                    sh """
                                    echo "${Dockerfile}" > Dockerfile
                                    docker build -t helloworld-phardzeyeu:${BUILD_NUMBER} .
                                    docker tag helloworld-phardzeyeu:${BUILD_NUMBER} nexus-dock.k8s.playpit.by:80/helloworld-phardzeyeu:${BUILD_NUMBER}
                                    docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                    docker push nexus-dock.k8s.playpit.by:80/helloworld-phardzeyeu:${BUILD_NUMBER}                             
                                    """
                                }
                            }
                        }
                    }
                }
        )
    }
}
