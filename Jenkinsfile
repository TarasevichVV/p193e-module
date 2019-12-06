def student="vtarasevich"
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
node {
    stage('Preparation (Checking out)'){
        checkout scm
    }
    stage('Building code'){
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
        withMaven(maven: 'M3', mavenSettingsFilePath: 'settings.xml') { 
            sh "mvn clean -f helloworld-project/helloworld-ws/pom.xml  install"
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
            withMaven(maven: 'M3', mavenSettingsFilePath: 'settings.xml') { 
            sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml  install'
            }
          },
          c: {
            sh 'echo "mvn post-integration-test"'  
          })
    }
    stage('Triggering job and fetching artefact after finishing'){
        build job: "MNTLAB-${student}-child1-job", parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]], wait: true
        copyArtifacts(projectName: "MNTLAB-${student}-child1-job");
    }
    stage('Packaging and Publishing results'){
        parallel(
            'Creating tar gz': {
                sh "tar xvzf  ${student}_dsl_script.tar.gz"
                sh "tar cvzf pipeline-${student}-${currentBuild.number}.tar.gz output.txt Jenkinsfile helloworld-project/helloworld-ws/target/helloworld-ws.war"
                },
            'Creating Docker Image':  {
                podTemplate(label: label,
                    containers: [
                        containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                        containerTemplate(name: 'docker', image: 'docker:18-dind', command: 'cat', ttyEnabled: true),
                    ],
                    volumes: [
                        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                        hostPathVolume(hostPath: '/home/workspace/TOPIC11/', mountPath: '/tmp')
                        
                    ]){
                        node(label) {
                            stage('Docker Build') {
                                container('docker') {
                                sh """
                                cp /tmp/helloworld-project/helloworld-ws/target/helloworld-ws.war .
                                echo "nexus-dock.k8s.playpit.by  "
                                echo "${Dockerfile}" > Dockerfile
                                docker build -t vtarasevich/app .
                                docker tag vtarasevich/app:latest 192.168.56.100:32133/vtarasevich/app:${BUILD_NUMBER}
                                docker login -u admin -p admin 192.168.56.100:32133
                                docker push 192.168.56.100:32133/vtarasevich/app:${BUILD_NUMBER}
                                """
                                }
                            }
                        }
                    }
                }
            )
        }
    }
