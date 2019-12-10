#!/usr/bin/env groovy

def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def label2 = "centos-jenkins-${UUID.randomUUID().toString()}"

String BRANCH_NAME='ayanchuk'


node {

    timestamps {

  


        stage ('Preparation (Checking out)') {
        git branch: 'ayanchuk', url: 'https://github.com/MNT-Lab/build-t00ls.git'
        }

        stage ('Building code') {
            sh '''
            sed -i "37i Build Number: $BUILD_NUMBER<br></p>" helloworld-project/helloworld-ws/src/main/webapp/index.html
            sed -i "37i BuldTime: $(date)<br>" helloworld-project/helloworld-ws/src/main/webapp/index.html
            sed -i "37i TriggeredBy: $(git log -1 --pretty=format:'%an')<br>" helloworld-project/helloworld-ws/src/main/webapp/index.html
            sed -i "37i <p>Artefact Version: 1.2.$BUILD_NUMBER<br>" helloworld-project/helloworld-ws/src/main/webapp/index.html
            '''
            withMaven(
                maven: 'M3'){
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
            copyArtifacts(projectName: 'MNTLAB-ayanchuk-child1-build-job', selector: lastCompleted())
            stash includes: "helloworld-project/helloworld-ws/target/helloworld-ws.war", name: "app"
        }
        stage ('Packaging and Publishing results') {
            parallel(
                'Create docker image': {
                    podTemplate(label: label,
                        containers: [
                                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                                    containerTemplate(name: 'docker', image: 'docker:18-dind', command: 'cat', ttyEnabled: true),
                            ],
                            volumes: [
                                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
//                                    hostPathVolume(hostPath: "${WORKSPACE}/helloworld-project/helloworld-ws/target/helloworld-ws.war", mountPath: '/helloworld-ws.war')
                            ])

                            {
                                node(label) {
                                    container('docker') {
                                        unstash "app"
                                        sh """
                                        cat <<EOF > /Dockerfile
                                        FROM tomcat:8.0
                                        COPY helloworld-project/helloworld-ws/target/helloworld-ws.war /usr/local/tomcat/webapps/
                                        """
                                        sh """docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                        docker build -f /Dockerfile -t nexus-dock.k8s.playpit.by:80/helloworld-ayanchuk:$BUILD_NUMBER .
                                        docker images
                                        docker push nexus-dock.k8s.playpit.by:80/helloworld-ayanchuk:$BUILD_NUMBER
                                        docker rmi nexus-dock.k8s.playpit.by:80/helloworld-ayanchuk:$BUILD_NUMBER
                                        """
                                    }

                                }
                            }



                },
                'Create tar.gz': {
                    sh 'echo "unzip"'
                    sh 'tar -xvzf script.tar.gz; ls -ahl'
                    sh 'echo "create gz"'
                    sh "tar -cvzf pipeline-ayanchuk-${currentBuild.number}.tar.gz output.txt -C /var/jenkins_home/workspace/EPBYMINW9146/mntlab-ci-pipeline@script/ Jenkinsfile -C ${WORKSPACE}/helloworld-project/helloworld-ws/target/ helloworld-ws.war" 
                    sh 'echo "deplpoy Artifact Archive"'
                    sh "curl -v -u admin:admin --upload-file pipeline-ayanchuk-${currentBuild.number}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/ayanchuk/${currentBuild.number}/pipeline-ayanchuk-${currentBuild.number}.tar.gz"
                }
            )
        }
        stage ('Asking for manual approval') {
            echo "Asking for manual approval"
//            timeout(time: 2, unit: "MINUTES") {
//                input message: "Approve Deploy $JOB_BASE_NAME / $BUILD_NUMBER ?", ok: 'Yes'
//            }
        }
        stage ('Deployment (rolling update, zero downtime)') {
            echo "Deployment (rolling update, zero downtime)"
            podTemplate(label: label2,
                containers: [
                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                    containerTemplate(name: 'centos', image: 'centos', command: 'cat', ttyEnabled: true),
                ],
                ) {
            node(label2) {
                stage('Deploy') {
                    container('centos') {
                        sh """
                        curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
                        chmod +x ./kubectl
                        mv ./kubectl /usr/local/bin/kubectl
                        kubectl version
                        """
                        sh """
                        curl https://raw.githubusercontent.com/MNT-Lab/p193e-module/ayanchuk/deploy.yaml --output deploy.yaml
                        sed -i "s|BUILD_NUMBER|${BUILD_NUMBER}|" deploy.yaml
                        kubectl apply -f deploy.yaml
                        """

                    }
                }
            }
            
        }
    }
        stage ('Implement handling  errors on each stage') {
            echo "Implement handling  errors on each stage"
        }
        stage ('Push functionality') {
            echo "Push functionality"
//            sh "kubectl get po -A"
        }
    }
}


