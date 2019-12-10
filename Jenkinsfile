def label = "docker-jenkins-${UUID.randomUUID().toString()}"
node {
    try {
        stage('check src to build') {
            git([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'amiasnikovich', credentialsId: '33c48519f78014a6f656a10b73d153cfa1da8f1e'])
        }
        stage('build artefact') {
            withMavenN(maven: 'M3') {          //BUG BUG BUG for test email
                sh 'mvn clean install -f clear_project/helloworld-ws/pom.xml'
            }
        }
//    stage('sonar scaner') {
//        scannerHome = tool 'Sonar'
//        withSonarQubeEnv('Sonar') {
//            sh "${scannerHome}/bin/sonar-scanner  -e -Dsonar.projectKey=mias -e -Dsonar.projectName=amiasnikovich -e -Dsonar.sources=clear_project/helloworld-ws/src -e -Dsonar.java.binaries=clear_project/helloworld-ws/target"
//        }
//    }
        stage('parallel testing') {

            parallel(
                    'kind-pre-integration-test': {
                        echo 'mvn pre-integration-test -f clear_project/helloworld-ws/pom.xml'
                    },
                    'integration-test': {
                        withSonarQubeEnv('Sonar') {
                            'mvn integration-test -f clear_project/helloworld-ws/pom.xml'
                        }
                    },
                    'kind-post-integration-test': {
                        'mvn post-integration-test -f clear_project/helloworld-ws/pom.xml'
                    }
            )
        }
        stage('build_child_job') {
            build job: 'DSL-jobs/MNTLAB-amiasnikovich-child1-build-job', parameters: [
                    string(name: 'BRANCH_NAME', value: 'amiasnikovich')
            ], wait: true

            copyArtifacts filter: '*.tar.gz', fingerprintArtifacts: true, projectName: 'DSL-jobs/MNTLAB-amiasnikovich-child1-build-job', target: 'Artifact'
        }
        stage('pack_and_pub_res') {
            parallel(
                    get_artifact: {
                        sh "tar -xvzf Artifact/amiasnikovich-script.tar.gz -C ./"
                        sh "cp clear_project/helloworld-ws/target/helloworld-ws.war ./"
                        sh "cp /var/jenkins_home/workspace/EPBYMINW9149/mntlab-ci-pipeline@script/Jenkinsfile ./"
                        sh "tar -czvf pipeline-amiasnikovich-${BUILD_NUMBER}.tar.gz output.txt helloworld-ws.war Jenkinsfile"
                        sh "curl -v -u admin:admin --upload-file pipeline-amiasnikovich-${BUILD_NUMBER}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/amiasnikovich/${BUILD_NUMBER}/pipeline-amiasnikovich-${BUILD_NUMBER}.tar.gz"

                        stash includes: "clear_project/helloworld-ws/target/helloworld-ws.war", name: "war"


                    },

                    create_container: {
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
                                container('docker') {
                                    unstash "war"
                                    sh ''' cat <<EOF > Dockerfile
                               FROM tomcat:8.0
                               COPY clear_project/helloworld-ws/target/helloworld-ws.war /usr/local/tomcat/webapps/
                               EXPOSE 8080
                               CMD ["catalina.sh", "run"]
                               '''
                                    sh '''
                                docker build -t nexus-dock.k8s.playpit.by:80/helloworld-amiasnikovich:$BUILD_NUMBER .
                                docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                docker push nexus-dock.k8s.playpit.by:80/helloworld-amiasnikovich:$BUILD_NUMBER
                                '''
                                }
                            }
                        }
                    }
            )
        }
        stage('approval') {
            timeout(time: 10, unit: 'MINUTES') {
                input(id: 'Deployment', message: 'Deploy or not?', ok: 'Deploy')
            }
        }
        stage('get_yaml_from_git') {
            git([url: 'https://github.com/MNT-Lab/p193e-module.git', branch: 'amiasnikovich', credentialsId: '33c48519f78014a6f656a10b73d153cfa1da8f1e'])
            stash includes: 'deployment.yaml', name: 'yaml'
        }

        stage('Deployment') {
            podTemplate(label: label,
                    containers: [
                            containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                            containerTemplate(name: 'kuber', image: 'lachlanevenson/k8s-kubectl', command: 'cat', ttyEnabled: true),
                    ],
                    volumes: [
                            hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                    ]
            ) {
                node(label) {
                    container('kuber') {
                        unstash "yaml"
                        sh '''
                        sed -i 's*helloworld-amiasnikovich*helloworld-amiasnikovich:'"$BUILD_NUMBER"'*' deployment.yaml
                        kubectl apply -f deployment.yaml
                        '''
                    }
                }
            }
        }

        currentBuild.result = 'SUCCESS'
        stage_name = 'All right'
    } catch (Exception err) {
        currentBuild.result = 'FAILURE'
        stage_name = env.STAGE_NAME + "failed"
        }

    finally {
        stage('Send email') {
            emailext attachLog: true, body: "build number is $BUILD_NUMBER, $JOB_NAME result is $currentBuild.result, $stage_name",
                    recipientProviders: [developers()],
                    subject: "$JOB_NAME result is $currentBuild.result",
                    to: 'al.miasnikovich@gmail.com'
        }
    }
}