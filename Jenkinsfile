def label = "docker-jenkins-${UUID.randomUUID().toString()}"
node {
    stage('check src to build') {
        git([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'amiasnikovich', credentialsId: '33c48519f78014a6f656a10b73d153cfa1da8f1e'])
    }
    stage('build artefact') {
        withMaven(maven: 'M3') {
            sh 'mvn clean install -f clear_project/helloworld-ws/pom.xml'
        }
    }
    stage('sonar scaner') {
        scannerHome = tool 'Sonar'
        withSonarQubeEnv('Sonar') {
            sh "${scannerHome}/bin/sonar-scanner  -e -Dsonar.projectKey=mias -e -Dsonar.projectName=amiasnikovich -e -Dsonar.sources=clear_project/helloworld-ws/src -e -Dsonar.java.binaries=clear_project/helloworld-ws/target"
        }
    }
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
}
