def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def label_deploy = "centos-jenkins-${UUID.randomUUID().toString()}"

node {
    stage('Preparation (Checking out)') {
        checkout([$class: 'GitSCM', branches: [[name: '*/anikitsenka']],
        userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]])
    }
    stage ('Build') {
        sh '''
        cat << EOF > helloworld-project/helloworld-ws/src/main/webapp/index.html
        </p>
        <b> Custom page by ANikitsenka </b>
        </p>
        <p> JOB_NAME = "$JOB_NAME" </p>
        <p> Created "$(date)" </p>
        <p> Minor version "$BUILD_ID" </p>
        EOF
        '''
        withMaven(maven: 'M3'){
            sh 'mvn clean verify -f helloworld-project/helloworld-ws/pom.xml'
            sh 'mvn package -f helloworld-project/helloworld-ws/pom.xml'
            stash includes: "helloworld-project/helloworld-ws/target/helloworld-ws.war", name: "binary_webapp"
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
                    stash includes: "deploy.yml", name: "Deployment"
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
                                docker build -t anikitsenka:${BUILD_ID} .
                                docker tag anikitsenka:${BUILD_ID} nexus-dock.k8s.playpit.by:80/anikitsenka:${BUILD_ID}
                                docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                docker push nexus-dock.k8s.playpit.by:80/anikitsenka:${BUILD_ID}
                                docker rmi nexus-dock.k8s.playpit.by:80/anikitsenka:${BUILD_ID}
                                '''
                            }
                        }
                    }
                }
                sh 'ls -lha'
            }
        )
    }
    stage ('Asking for manual approval') {
    timeout(time: 3, unit: "MINUTES") { input message: 'U r brave, are not u?', ok: 'Yes' }
    }
    stage ('Deployment (rolling update, zero downtime)') {
        podTemplate(label: label_deploy,
                containers: [
                        containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                        containerTemplate(name: 'centos', image: 'centos', command: 'cat', ttyEnabled: true),
                ],
        ) {
            node(label_deploy) {
                    container('centos') {
                        unstash "Deployment"
                        sh """
                        curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
                        chmod +x ./kubectl
                        mv ./kubectl /usr/local/bin/kubectl
                        sed -i "s/BUILD_NUMBER/${BUILD_NUMBER}/g" deploy.yml
                        kubectl apply -f deploy.yml
                        """
                    }
                }
        }
    }
}
