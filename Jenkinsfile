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
            sh """
            sed -i "37i Build Number: $BUILD_NUMBER<br></p>" helloworld-project/helloworld-ws/src/main/webapp/index.html
            sed -i "37i BuldTime: $(date)<br>" helloworld-project/helloworld-ws/src/main/webapp/index.html
            sed -i "37i TriggeredBy: $(git log -1 --pretty=format:'%an')<br>" helloworld-project/helloworld-ws/src/main/webapp/index.html
            sed -i "37i <p>Artefact Version: 1.2.$BUILD_NUMBER<br>" helloworld-project/helloworld-ws/src/main/webapp/index.html
            """
            withMaven(
                maven: 'M3'){
//            sh "pwd"
//            sh "ls -ahl"
//            sh "find $JENKINS_HOME -name *Jenkinsfile*"
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
                                        FROM alpine
                                        RUN apk update && apk add wget tar openjdk8 && \
                                        wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.20/bin/apache-tomcat-8.5.20.tar.gz && \
                                        tar -xvf apache-tomcat-8.5.20.tar.gz && \
                                        mkdir /opt/tomcat && \
                                        mv apache-tomcat*/* /opt/tomcat/
                                        COPY helloworld-project/helloworld-ws/target/helloworld-ws.war /opt/tomcat/webapps/
                                        EXPOSE 8080
                                        HEALTHCHECK CMD curl --fail http://localhost:8080/ || exit 1
                                        CMD ["/opt/tomcat/bin/catalina.sh", "run"]
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
                        yum install -y yum-utils
                        cat << EOF > /etc/yum.repos.d/kubernetes.repo
                        [kubernetes]
                        name=Kubernetes
                        baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-x86_64
                        enabled=1
                        gpgcheck=1
                        repo_gpgcheck=1
                        gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
                         EOF
                        """
                        sh """
                        yum install -y kubectl
                        cat <<EOF | kubectl apply -f -
                        apiVersion: v1
                        kind: Namespace
                        metadata:
                          name: ayanchuk
                        ---
                        apiVersion: v1
                        data:
                          .dockerconfigjson: eyJhdXRocyI6eyJuZXh1cy1kb2NrLms4cy5wbGF5cGl0LmJ5OjgwIjp7InVzZXJuYW1lIjoiYWRtaW4iLCJwYXNzd29yZCI6ImFkbWluIiwiYXV0aCI6IllXUnRhVzQ2WVdSdGFXND0ifX19
                        kind: Secret
                        metadata:
                          name: docker-secret
                          namespace: ayanchuk
                        type: kubernetes.io/dockerconfigjson
                        ---
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          name: application
                          namespace: ayanchuk
                        spec:
                          replicas: 1
                          strategy:
                            type: RollingUpdate
                            rollingUpdate:
                              maxSurge: 1
                              maxUnavailable: 25%
                        selector:
                            matchLabels:
                            app: application
                        template:
                            metadata:
                            labels:
                                app: application
                            spec:
                            containers:
                            - name: hello-app
                                image: nexus-dock.k8s.playpit.by:80/helloworld-shanchar:$BUILD_NUMBER
                                ports:
                                - containerPort: 8080
                                readinessProbe:
                                httpGet:
                                    path: /
                                    port: 8080
                                    initialDelaySeconds: 5
                                    periodSeconds: 5
                                    successThreshold: 1
                                livenessProbe:
                                    httpGet:
                                    path: /helloworld-ws
                                    port: 8080          
                            imagePullSecrets:
                            - name: docker-secret   
                        ---
                        apiVersion: v1
                        kind: Service
                        metadata:
                          name: application-svc
                          namespace: ayanchuk
                          labels:
                            app: application
                        spec:
                          ports:
                          - name: application-svc
                            port: 80
                            targetPort: 8080
                            protocol: TCP
                          selector:
                            app: application
                          type: ClusterIP
                        ---
                        apiVersion: extensions/v1beta1
                        kind: Ingress
                        metadata:
                          name: application-ingress
                          namespace: ayanchuk
                          annotations:
                            nginx.org/rewrites: serviceName=application-svc rewrite=/helloworld-ws/
                        spec:
                          rules:
                          - host: ayanchuk-app.k8s.playpit.by
                            http:
                              paths: 
                              - path: /
                                backend:
                                  serviceName: application-svc
                                  servicePort: application-svc
                        EOF
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


