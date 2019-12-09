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
        git ([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'skudrenko'])
        withEnv(maven: 'M3') {
            sh 'mvn clean install -f helloworld-project/helloworld-ws/pom.xml'
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
                                sh '"$MVN_HOME/bin/mvn" -f helloworld-project/helloworld-ws/pom.xml integration-test'
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
                        copyArtifacts filter: 'output.txt', flatten: true, projectName: 'MNTLAB-skudrenko-child1-build-job', selector: workspace()
    }

//stage ('Push the Artifact to Nexus') {
//    parallel(
//        'Generating TAR archive'
//                        sh 'tar -zcvf pipeline-skudrenko-${BUILD_NUMBER}.tar.gz output.txt Jenkinsfile ./helloworld-ws/target/helloworld-ws.war'


}


