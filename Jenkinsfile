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
            sh "ls $JENKINS_HOME/workspace/mntlab-ci-pipeline/EPBYMINW9146"
            sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
            }
        }

        stage ('Sonar scan') {
            def scannerHome = tool 'Sonar';
            withSonarQubeEnv('Sonar'){
                sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=ayanchuk:helloworld -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }
        }

        stage ('Testing') {
            parallel(
                'pre-integration-test': {
                    sh 'echo "mvn pre-integration-test"'
                },
                'integration-test': {
                    withMaven(
                        maven: 'M3'){
                            sh "mvn -f helloworld-project/helloworld-ws/pom.xml integration-test"
                        }
                    
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
                    sh 'tar -xvzf script.tar.gz; tar -czf "pipeline-ayanchuk-$BUILD_NUMBER.tar.gz" "$JENKINS_HOME/workspace/mntlab-ci-pipeline/EPBYMINW9146/Jenkinsfile" output.txt -C helloworld-project/helloworld-ws/target helloworld-ws.war'
                    sh 'echo "Create Artifact Archive"'
                    sh 'curl -v -u admin:admin --upload-file "pipeline-ayanchuk-$BUILD_NUMBER.tar.gz" "http://nexus-service.jenkins.svc.cluster.local:8081/repository/artifacts/pipeline-ayanchuk-$BUILD_NUMBER.tar.gz"'
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


