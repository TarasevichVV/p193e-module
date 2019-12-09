def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def Dockerfile = """  FROM alpine
                    RUN apk update && apk add wget tar openjdk8 && \
                    wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.20/bin/apache-tomcat-8.5.20.tar.gz && \
                    tar -xvf apache-tomcat-8.5.20.tar.gz && \
                    mkdir /opt/tomcat && \
                    mv apache-tomcat*/* /opt/tomcat/
                    COPY helloworld-project/helloworld-ws/target/helloworld-ws.war /opt/tomcat/webapps
                    EXPOSE 8080
                    
                    CMD ["/opt/tomcat/bin/catalina.sh", "run"]
                 """
node {
   stage('Preparation') {
      checkout scm
      // git branch: 'skudrenko', url: 'https://github.com/MNT-Lab/build-t00ls.git'
   }
   stage('Build code') {
        git ([url: 'https://github.com/MNT-Lab/p193e-module.git', branch: 'skudrenko'])
        stash includes: "Jenkinsfile", name: "jkf"
        git ([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'skudrenko'])
        withMaven(maven: 'M3') {
            sh 'mvn clean install -f helloworld-project/helloworld-ws/pom.xml'
            stash includes: "helloworld-project/helloworld-ws/target/helloworld-ws.war", name: "war"
//            stash includes: "tomcat.yaml", name: "deploy"
//            stash includes: "Dockerfile", name: "Dockerfile"
         }
      }

   stage('Testing Phase I (Sonar)'){
        def scannerHome = tool 'Sonar';
        withSonarQubeEnv(){
            sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=skudrenko -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
        }
    }

   stage ('Testing Phase II (Unit)') {
        parallel(
                'Pre Integration': {
                        withMaven(maven: 'M3') {
                                sh '''
                                echo "mvn pre-integration-test"
                                '''
                        }
                },
                'Integration': {
                        withMaven(maven: 'M3') {
                                sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml'
                        }
                },
                'Post Integration': {
                        withMaven(maven: 'M3') {
                                sh '''
                                echo "mvn post-integration-test"
                                '''
                        }
                }
        )
    }

    stage ('Triggering job and fetching artifact after finishing') {
                        build job: 'MNTLAB-skudrenko-child1-build-job', parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: 'skudrenko']], wait: true;
                        copyArtifacts(projectName: 'MNTLAB-skudrenko-child1-build-job', selector: lastSuccessful())
    }

stage('Packaging and Publishing results'){
        parallel(
            'Creating tar gz': {
                unstash "jkf"
                sh '''
                tar xvzf  skudrenko_dsl_script.tar.gz
                tar cvzf pipeline-skudrenko-${BUILD_NUMBER}.tar.gz output.txt Jenkinsfile helloworld-project/helloworld-ws/target/helloworld-ws.war
                curl -v -u admin:admin --upload-file pipeline-skudrenko-${BUILD_NUMBER}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/skudrenko/${BUILD_NUMBER}/pipeline-skudrenko-${BUILD_NUMBER}.tar.gz
                '''
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
                            stage('docker_build') {
                                container('docker') {
                                    unstash "war"
                                    sh """
                                    echo "${Dockerfile}" > Dockerfile
                                    docker build -t helloworld-skudrenko:${BUILD_NUMBER} .
                                    docker tag helloworld-skudrenko:${BUILD_NUMBER} nexus-dock.k8s.playpit.by:80/helloworld-skudrenko:${BUILD_NUMBER}
                                    docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                    docker push nexus-dock.k8s.playpit.by:80/helloworld-skudrenko:${BUILD_NUMBER}                             
                                    """
                                }
                            }
                        }
                    }
                }
            )
        }
}
