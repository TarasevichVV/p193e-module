def student="vtarasevich"
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def Dockerfile='''  From alpine
                                
                    RUN apk update && apk add wget tar openjdk8 && \
                        wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.20/bin/apache-tomcat-8.5.20.tar.gz && \
                        tar -xvf apache-tomcat-8.5.20.tar.gz && \
                        mkdir /opt/tomcat && \
                        mv apache-tomcat*/* /opt/tomcat/
                                
                        COPY helloworld-project/helloworld-ws/target/helloworld-ws.war /opt/tomcat/webapps
                                
                        EXPOSE 8080
                        CMD ["/opt/tomcat/bin/catalina.sh", "run"]'''
def label-deploy = "centos-jenkins-${UUID.randomUUID().toString()}"
node {
    stage('Preparation (Checking out)'){
        checkout scm
    }
    stage('Building code'){
        sh "echo $WORKSPACE"
        git branch: 'vtarasevich', url: 'https://github.com/MNT-Lab/build-t00ls'
        sh label: '', script: '''TimeStamp=$(date)
                                cat << EOF > helloworld-project/helloworld-ws/src/main/webapp/index.html
                                <!DOCTYPE html>
                                <html>
                                <body>
                                <h1>author=LUNTIK</h1>
                                <p>build_number=${BUILD_NUMBER}</p>
                                <p>buildTime=$TimeStamp</p>
                                </body>
                                </html>
                                EOF'''
        withMaven(maven: 'M3') { 
            sh "mvn clean -f helloworld-project/helloworld-ws/pom.xml  install"
            stash includes: "helloworld-project/helloworld-ws/target/helloworld-ws.war", name: "war"
            stash includes: "tomcat.yaml", name: "deploy"
        }
    }
    stage('Sonar scan'){
        def scannerHome = tool 'Sonar';
        withSonarQubeEnv('Sonar'){
            sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=vtarasevich -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
        }
    }
    stage('Testing') {
        parallel(
          a: {
            sh 'echo "mvn pre-integration-test"'
          },
          b: {
            withMaven(maven: 'M3') { 
            sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml  install'
            }
          },
          c: {
            sh 'echo "mvn post-integration-test"'  
          })
    }
    stage('Triggering job and fetching artefact after finishing'){
        build job: "MNTLAB-${student}-child1-build-job", parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]], wait: true
        copyArtifacts(projectName: "MNTLAB-${student}-child1-build-job");
    }
    stage('Packaging and Publishing results'){
        parallel(
            'Creating tar gz': {
                sh "tar xvzf  ${student}_dsl_script.tar.gz"
                sh "tar cvzf pipeline-${student}-${currentBuild.number}.tar.gz output.txt Jenkinsfile helloworld-project/helloworld-ws/target/helloworld-ws.war"
                sh "curl -v -u admin:admin --upload-file pipeline-${student}-${currentBuild.number}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${currentBuild.number}/pipeline-${student}-${currentBuild.number}.tar.gz"
                },
            'Creating Docker Image':  {
                podTemplate(label: label,
                    containers: [
                        containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                        containerTemplate(name: 'docker', image: 'docker:18-dind', command: 'cat', ttyEnabled: true),
                    ],
                    volumes: [
                        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')
                    ]){
                        node(label) {
                            stage('Docker Build') {
                                container('docker') {
                                unstash "war"
                                sh 'ls'
                                sh """
                                echo "${Dockerfile}" > Dockerfile
                                docker build -t vtarasevich/app .
                                docker tag vtarasevich/app:latest nexus-dock.k8s.playpit.by:80/vtarasevich/app:${currentBuild.number}
                                docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                docker push nexus-dock.k8s.playpit.by:80/vtarasevich/app:${currentBuild.number}
                                """
                                }
                            }
                        }
                    }
                }
            )
        }
     stage("Asking for manual approval") {
         stage = env.STAGE_NAME
         timeout(time: 5, unit: "MINUTES") {
             input message: 'Please, approve deploy tomcat', ok: 'Approve'
         }
     }
     stage('Deployment (rolling update, zero downtime)') {
        stage = env.STAGE_NAME
            podTemplate(label: label2,
                containers: [
                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                    containerTemplate(name: 'centos', image: 'centos', ttyEnabled: true)
                ]
            )
            {
                node(label-deploy) {
                    container('centos') {
                        unstash "deploy"
                        sh """
                        curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
                        chmod +x ./kubectl
                        mv ./kubectl /usr/local/bin/kubectl
                        sed -i "s/IMAGE/nexus-dock.k8s.playpit.by:80/vtarasevich/app:${currentBuild.number}/" tomcat.yaml
                        kubectl apply -f docker-deploy.yml
                        """
                    }
                }
            }
        }
}
