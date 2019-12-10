#!/usr/bin/env groovy
student = 'melizarov'
mails_to_notify = 'mk-ez@mmmk.com'
node {
    try {
        stage('1-Checkout') {
            checkout scm
            echo "checkout from dev branch"
        }

        stage('2-Build') {
            git branch: "${student}", url: 'https://github.com/MNT-Lab/build-t00ls.git'
            sh """
cat <<EOF >> helloworld-project/helloworld-ws/src/main/webapp/index.html
<p> Author = "${student}" </p>
<p> Job_Name = "$JOB_NAME" </p>
<p> Build_Date = `date '+%A %d-%B, %Y'` </p>
<p> VERSION = 0.1."$BUILD_NUMBER" </p>
EOF
"""
            withMaven(maven: "M3") { //maven3-6-3
                sh "mvn -f helloworld-project/helloworld-ws/pom.xml package"
                sh "echo --------------------final 1---------------------------"
            }
        }
/*        stage('3-Sonar') {
            //   environment {
            def scannerHome = tool 'Sonar'
            //  }
            withSonarQubeEnv('Sonar') {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${student} -e       -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }
            *//*        timeout(time: 10, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true

            }*//*
        }*/
/*        stage('4-Tests') {
            parallel(
                '4-1-preintegration': {
                    sh 'echo \'mvn pre-integration-test\''
                },
                '4-2-integrationtest': {
                    sh 'echo \'This is integration-test\''
                    withMaven(maven: "M3") {
                        sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml'
                    }
                },
                '4-3-postintegration': {
                    sh 'echo \'mvn post-integration-test\''
                }
            )
        }*/
/*        stage('5-Triggering') {
            sh "echo --------------------start 5---------------------------"
            build job: "MNTLAB-${student}-child1-build-job", parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]], wait: true
            copyArtifacts fingerprintArtifacts: true, projectName: "MNTLAB-${student}-child1-build-job", selector: lastSuccessful()
            //sh 'find / -name output.txt'
            sh "echo trigering"
        }*/
        stage('6-Archiving') {
            parallel(
                '6-1-archiving_artifact': {
                    //sh 'find / -name Jenkinsfile -type f -exec ls -al {} '
                    //sh 'find / -name output.txt'
                    //sh 'find / -name *.war'
                    sh """
tar zxvf ${student}_dsl_script.tar.gz
cp /var/jenkins_home/workspace/EPBYMINW6852/mntlab-ci-pipeline@script/Jenkinsfile ./
cp helloworld-project/helloworld-ws/target/helloworld-ws.war ./
tar czf pipeline-${student}-${BUILD_NUMBER}.tar.gz output.txt helloworld-ws.war Jenkinsfile
curl -v -u admin:admin --upload-file pipeline-${student}-${BUILD_NUMBER}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${BUILD_NUMBER}/pipeline-${student}-${BUILD_NUMBER}.tar.gz
"""
                },
                '6-2-creating_docker': {

                    sh "echo '-----------------------------------------docker build list dockerfile--------------------------------------------'"
                    sh "ls -al Dockerfile"
                    def label = "docker-jenkins-${UUID.randomUUID().toString()}"

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
                            stage('6-2-1-Docker Build') {
                                container('docker') {
                                    sh """
cat > Dockerfile <<EOF
FROM tomcat
RUN curl -u admin:admin -o pipeline-${student}-${BUILD_NUMBER}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${BUILD_NUMBER}/pipeline-${student}-${BUILD_NUMBER}.tar.gz -L && \
tar -xvf pipeline-${student}-${BUILD_NUMBER}.tar.gz &&\
mv helloworld-ws.war /usr/local/tomcat/webapps
CMD bash /usr/local/tomcat/bin/catalina.sh run
EOF

#docker build . -t helloworld-${student}:${BUILD_NUMBER}
#docker "tag helloworld-${student}:${BUILD_NUMBER}" "http://nexus.k8s.playpit.by/repository/docker/${student}:${BUILD_NUMBER}"
#docker push http://nexus.k8s.playpit.by/repository/docker/${student}:${BUILD_NUMBER}

cat Dockerfile  
""";
/*

                                    sh "echo '6-2-1--a: dockerfile:'"
                                    sh "ls -al Dockerfile"
                                    sh "echo '6-2-1--b:  Building docker image...---tomcat_${student}----'"
*/

                                    sh """
docker build . -t helloworld-${student}:${BUILD_NUMBER}
docker tag "helloworld-${student}:${BUILD_NUMBER}" "http://nexus.k8s.playpit.by/repository/docker/helloworld-${student}:${BUILD_NUMBER}"
docker login -u admin -p admin nexus-dock.k8s.playpit.by
docker push http://nexus.k8s.playpit.by/repository/docker/helloworld-${student}:${BUILD_NUMBER}
docker rmi helloworld-${student}:${BUILD_NUMBER}"
"""
                                }
                            }
                        }
                    }
                 }
                    )
                        // }
//            nexusArtifactUploader {
//                nexusVersion('nexus2')
//                protocol('http')
//                nexusUrl('nexus.k8s.playpit.by')
//                groupId("{$student}")
//                version('0.1')
//                repository("maven-releases/app/${student}/${BUILD_NUMBER}")
//                credentialsId('nexus-cred')
//                artifact {
//                    artifactId('GZIP')
//                    type('gzip')
//                    classifier('')
//                    file('pipeline-${student}-${BUILD_NUMBER}.tar.gz')
//                }
//                //nexusArtifactUploader artifacts: [[artifactId: 'GZIP', classifier: '', file: 'pipeline-${student}-${BUILD_NUMBER}.tar.gz', type: 'tar.gz']], credentialsId: 'nexus-cred', groupId: 'Hllo_ws', nexusUrl: 'nexus.k8s.playpit.by', nexusVersion: 'nexus2', protocol: 'http', repository: 'maven-releases/app/${student}/${BUILD_NUMBER}/', version: '0.1'}
//            }

        }
        stage('7-Asking approval') {

            sh "echo Asking"
            timeout (time:1, unit:'MINUTES') {
                input "Approve deploy to prod?", ok: 'Yes'
            }
        }
        stage('8-Deploy') {
            sh "echo deploy"

        }

        } catch (e) {
            String error = "${e}";
            emailext body: 'Error mesage: ${error}\n\n<br>Project: ${env.JOB_NAME} </br> Build Number: ${env.BUILD_NUMBER} <br> URL build: ${env.BUILD_URL}" </br> Stage: {env.STAGE_NAME};',
                    subject: 'Jenkins Error ${env.JOB_NAME} ',
                    to: 'mk.elz@bk.com'
                }
    }