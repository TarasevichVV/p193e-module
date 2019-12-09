#!/usr/bin/env groovy

def label = "docker-jenkins-${UUID.randomUUID().toString()}"

node {
    stage ('checking_out') {
        checkout scm
    }
    stage ('building_code') {
        git ([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
        sh '''
        cat << EOF >> helloworld-project/helloworld-ws/src/main/webapp/index.html
        <p> AUTHOR = phardzeyeu </p>
        <p> JOB_NAME = "$JOB_NAME" </p>
        <p> COMMIT_ID = "$GIT_COMMIT" </p>
        <p> BUILD_TIME = "$(date)" </p>
        <p> ARTIFACT_VERSION = 1.0."$BUILD_NUMBER" </p>
        EOF
        '''
        withMaven(maven: 'M3') {
            sh 'mvn clean install -f helloworld-project/helloworld-ws/pom.xml'
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
    stage ('archiving_artifact') {
                    sh """
                    tar zxvf phardzeyeu_dsl_script.tar.gz
                    cp helloworld-project/helloworld-ws/target/helloworld-ws.war .
                    tar czf pipeline-phardzeyeu-${BUILD_NUMBER}.tar.gz output.txt helloworld-ws.war
                    """
                }
    stage ('packaging_and_publishing_results'){
        parallel (
                'archiving_artifact' : {
                    sh """
                    tar zxvf phardzeyeu_dsl_script.tar.gz
                    cp helloworld-project/helloworld-ws/target/helloworld-ws.war .
                    tar czf pipeline-phardzeyeu-${BUILD_NUMBER}.tar.gz output.txt helloworld-ws.war
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
                            stage('Docker Build') {
                                container('docker') {
                                    echo "Building docker image..."
                                    sh """
                                    docker build -t helloworld-phardzeyeu:${BUILD_NUMBER} .
                                    """
                                    // docker tag helloworld-phardzeyeu:${BUILD_NUMBER} 192.168.56.66:32389/phardzeyeu/tomcat:$BUILD_NUMBER
                                    // docker login -u admin -p admin123 192.168.56.66:32389
                                    // docker push 192.168.56.66:32389/phardzeyeu/tomcat:$BUILD_NUMBER
                                }
                            }
                        }
                    }
                }
        )
    }
}
