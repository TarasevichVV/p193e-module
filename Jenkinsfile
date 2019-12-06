#!/usr/bin/env groovy

def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def Dockerfile='''  From alpine
                                
                    RUN apk update && apk add wget tar openjdk8 && \
                        wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.20/bin/apache-tomcat-8.5.20.tar.gz && \
                        tar -xvf apache-tomcat-8.5.20.tar.gz && \
                        mkdir /opt/tomcat && \
                        mv apache-tomcat*/* /opt/tomcat/
                                
                        COPY helloworld-ws.war /opt/tomcat/webapps
                                
                        EXPOSE 8080
                        CMD ["/opt/tomcat/bin/catalina.sh", "run"]'''

String BRANCH_NAME='ayanchuk'


node {

    timestamps {

  


        stage ('Preparation (Checking out)') {
        git branch: 'ayanchuk', url: 'https://github.com/MNT-Lab/build-t00ls.git'
        }

        stage ('Building code') {
            withMaven(
                maven: 'M3'){
            sh "pwd"
            sh "ls"
            sh "find $JENKINS_HOME -name Jenkinsfile"
//            sh "ls $JENKINS_HOME/workspace/mntlab-ci-pipeline/EPBYMINW9146"
            sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
            }
        }

        stage ('Sonar scan') {
//            def scannerHome = tool 'Sonar';
//            withSonarQubeEnv('Sonar'){
//                sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=ayanchuk:helloworld -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
//            }
        }

        stage ('Testing') {
            parallel(
                'pre-integration-test': {
                    sh 'echo "mvn pre-integration-test"'
                },
                'integration-test': {
//                    withMaven(
//                        maven: 'M3'){
//                            sh "mvn -f helloworld-project/helloworld-ws/pom.xml integration-test"
//                       }
                    
                    sh 'echo "mvn integration-test"'
                },
                'post-integration-test': {
                    sh 'echo "mvn mvn post-integration-test"'
                }
            )
        }
        stage ('Triggering job and fetching artefact after finishing') {
            failFast: false
            blocking: true
            build job: 'MNTLAB-ayanchuk-child1-build-job', parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "$BRANCH_NAME"]]
            copyArtifacts(projectName: 'MNTLAB-ayanchuk-child1-build-job')
        }
        stage ('Packaging and Publishing results') {
            parallel(
                'Create docker image': {
                    sh 'echo "Create Docker Image"'
                },
                'Create tar.gz': {
                    sh 'echo "unzip"'
                    sh 'tar -xvzf script.tar.gz; ls'
                    sh "find ./ -name helloworld-ws.war"
//                    sh 'tar -cvf "pipeline-ayanchuk-${currentBuild.number}.tar" -C helloworld-project/helloworld-ws/target helloworld-ws.war'
//                    sh 'tar -rf "pipeline-ayanchuk-${currentBuild.number}.tar" -C /var/jenkins_home/workspace/EPBYMINW9146/mntlab-ci-dsl Jenkinsfile'
//                    sh 'tar -rf "pipeline-ayanchuk-${currentBuild.number}.tar" output.txt ; gzip "pipeline-ayanchuk-${currentBuild.number}.tar"'
                    sh 'echo "create gz"'
                    sh "tar -cvzf pipeline-ayanchuk-${currentBuild.number}.tar.gz output.txt -C /var/jenkins_home/workspace/EPBYMINW9146/mntlab-ci-dsl@script/ Jenkinsfile -C ${WORKSPACE}/helloworld-project/helloworld-ws/target/ helloworld-ws.war" 
//                    -C /var/jenkins_home/workspace/EPBYMINW9146/mntlab-ci-dsl/ Jenkinsfile'
                    sh 'echo "deplpoy Artifact Archive"'
                    sh "curl -v -u admin:admin --upload-file pipeline-ayanchuk-${currentBuild.number}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/ayanchuk/${currentBuild.number}/pipeline-ayanchuk-${currentBuild.number}.tar.gz"
                }
            )
        }
        stage ('Asking for manual approval') {
            echo "Asking for manual approval"
        }
        stage ('Deployment (rolling update, zero downtime)') {
            echo "Deployment (rolling update, zero downtime)"
        }
        stage ('Implement handling  errors on each stage') {
            echo "Implement handling  errors on each stage"
        }
        stage ('Push functionality') {
            echo "Push functionality"
        }
    }
}


