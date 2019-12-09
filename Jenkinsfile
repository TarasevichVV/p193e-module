#!/usr/bin/env groovy

def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def label2 = "centos-jenkins-${UUID.randomUUID().toString()}"

node {
    stage ('1.checking_out') {
        checkout scm
    }
    stage ('2.building_code') {
        git ([url: 'https://github.com/MNT-Lab/p193e-module.git', branch: 'phardzeyeu'])
        stash includes: "tomcat.yml", name: "tomcat"
        stash includes: "Dockerfile", name: "docker"
        stash includes: "Jenkinsfile", name: "jfile"
        git ([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'phardzeyeu'])
        def myinfo = """
        <p> AUTHOR = phardzeyeu </p>
        <p> JOB_NAME = $JOB_NAME </p>
        <p> COMMIT_ID = $GIT_COMMIT </p>
        <p> BUILD_TIME = $(date) </p>
        <p> ARTIFACT_VERSION = 1.0.$BUILD_NUMBER </p>
        """
        sh "echo ${myinfo} >> helloworld-project/helloworld-ws/src/main/webapp/index.html"
        withMaven(maven: 'M3') {
            sh 'mvn clean install -f helloworld-project/helloworld-ws/pom.xml'
            stash includes: "helloworld-project/helloworld-ws/target/helloworld-ws.war", name: "warka"
    }
    }
    /*
    stage ('3.sonar_scan') {
    def scannerHome = tool 'Sonar'
    withSonarQubeEnv() {
        sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=phardzeyeu -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target -e -Dsonar.sources=helloworld-project/helloworld-ws/src" 
        }
    }
    stage ('4.testing') {
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
    */    
    stage ('5.triggering_job') {
        build job: 'MNTLAB-phardzeyeu-child1-build-job', parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: 'phardzeyeu']], wait: true;
        copyArtifacts(projectName: 'MNTLAB-phardzeyeu-child1-build-job', selector: lastSuccessful())
    }
    stage ('6.packaging_and_publishing_results'){
        parallel (
                'archiving_artifact' : {
                    unstash "jfile"
                    sh """
                    tar zxvf phardzeyeu_dsl_script.tar.gz
                    cp helloworld-project/helloworld-ws/target/helloworld-ws.war .
                    tar czf pipeline-phardzeyeu-${BUILD_NUMBER}.tar.gz output.txt helloworld-ws.war Jenkinsfile
                    curl -v -u admin:admin --upload-file pipeline-phardzeyeu-${BUILD_NUMBER}.tar.gz \
                    nexus.k8s.playpit.by/repository/maven-releases/app/phardzeyeu/${BUILD_NUMBER}/pipeline-phardzeyeu-${BUILD_NUMBER}.tar.gz
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
                            stage('6.1.docker_build') {
                                container('docker') {
                                    unstash "docker"
                                    unstash "warka"
                                    sh """
                                    docker build -t helloworld-phardzeyeu:${BUILD_NUMBER} .
                                    docker tag helloworld-phardzeyeu:${BUILD_NUMBER} nexus-dock.k8s.playpit.by:80/helloworld-phardzeyeu:${BUILD_NUMBER}
                                    docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                    docker push nexus-dock.k8s.playpit.by:80/helloworld-phardzeyeu:${BUILD_NUMBER}
                                    docker rmi nexus-dock.k8s.playpit.by:80/helloworld-phardzeyeu:${BUILD_NUMBER}
                                    """
                                }
                            }
                        }
                    }
                }
        )
    }
    /*
    stage ('7.asking_for_manual_approval') {
        timeout(time: 5, unit: "MINUTES") {
            input message: 'Do you want to approve the deploy in production?', ok: 'Yes'
        }
    }
    */
    stage ('8.deployment') {
        podTemplate(label: label2,
                    containers: [
                                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                                    containerTemplate(name: 'centos', image: 'centos', command: 'cat', ttyEnabled: true),
                                ],                                
                               ) {
            node(label2) {
                stage('8.1.deployment') {
                    container('centos') {
                        unstash "tomcat"
                        sh """
                        curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
                        chmod +x ./kubectl
                        mv ./kubectl /usr/local/bin/kubectl
                        sed -i "s/BUILD_NUMBER/${BUILD_NUMBER}/g" tomcat.yml
                        kubectl apply -f tomcat.yml
                        """
                    }
                }
            }
        }
    }
}
