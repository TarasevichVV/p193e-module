def label = "docker-jenkins-${UUID.randomUUID().toString()}"

node {
    stage('Preparation (Checking out)') {
        checkout([$class: 'GitSCM', branches: [[name: '*/anikitsenka']],
        userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]])
    }
    stage ('Build') {
        withMaven(maven: 'M3'){
            sh 'mvn clean verify -f helloworld-project/helloworld-ws/pom.xml'
            sh 'mvn package -f helloworld-project/helloworld-ws/pom.xml'
            stash includes "helloworld-project/helloworld-ws/target/helloworld-ws.war", name: "binary_webapp"
        }
    }
    stage('Sonar scan'){
        def scannerHome = tool 'Sonar';
        withSonarQubeEnv(){
            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=anikitsenka -Dsonar.sources=helloworld-project/helloworld-ws/src -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
        }
    }
    stage('Testing'){
        parallel (
            test1: {
                withMaven(maven: 'M3'){
                    sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml'
                }
            },
            test2: { sh 'echo "preintegration-test"' },
            test3: { sh 'echo "postintegration-test"' }
        )
    }
    stage('Triggering job and fetching artefact after finishing'){
        build job: "MNTLAB-anikitsenka-child1-build-job", parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "anikitsenka"]], wait: true;
        copyArtifacts(filter:'*', projectName: 'MNTLAB-anikitsenka-child1-build-job', selector: lastSuccessful());
        sh 'ls -lha'
    }
    stage('Packaging and Publishing results'){
        parallel (
            arch: {
                sh '''
                    tar -zxf anikitsenka_dsl_script.tar.gz output.txt
                    tar -czf pipeline-anikitsenka-${BUILD_NUMBER}.tar.gz output.txt helloworld-project/helloworld-ws/target/helloworld-ws.war
                    ls -lha
                '''
                stash includes: "pipeline-anikitsenka-${BUILD_NUMBER}.tar.gz", name: "artefact_targz"
            },
            dock: {
                checkout([$class: 'GitSCM', branches: [[name: '*/anikitsenka']],
                    userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/p193e-module.git']]])
                    stash includes: "Dockerfile", name: "Dockerfile"
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
                                sh 'echo "Building docker image..."'
                                unstash "Dockerfile"
                                unstash "binary_webapp"
                                sh '''
                                ls -lha
                                docker build -t anikitsenka/tomcat .
                                docker tag anikitsenka/tomcat 192.168.56.106:30083/anikitsenka/tomcat:${BUILD_ID}
                                docker version
                                '''
                            }
                        }
                    }
                }
                    // docker build -t anikitsenka/tomcat .
                    // docker tag anikitsenka/tomcat 192.168.56.106:30083/anikitsenka/tomcat:${BUILD_ID}
                    // docker login -u admin -p nexus 192.168.56.106:30083
                    // docker push 192.168.56.106:30083/anikitsenka/tomcat:${BUILD_ID}
                sh 'ls -lha'
            }
        )
    }
}
