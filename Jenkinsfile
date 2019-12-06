
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def Dockerfile = '''  From alpine
                                
                    RUN apk update && apk add wget tar openjdk8 && \
                        wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.20/bin/apache-tomcat-8.5.20.tar.gz && \
                        tar -xvf apache-tomcat-8.5.20.tar.gz && \
                        mkdir /opt/tomcat && \
                        mv apache-tomcat*/* /opt/tomcat/
                                
                        COPY helloworld-ws.war /opt/tomcat/webapps
                                
                        EXPOSE 8080
                        CMD ["/opt/tomcat/bin/catalina.sh", "run"]'''



node ('master') {
    def student = "shanchar"

    stage ('Preparation (Checking out)') {
      checkout scm
    }
    
    stage ('Building code') {
      git branch: "${student}", url: 'https://github.com/MNT-Lab/build-t00ls.git'
      withMaven(maven: 'M3') {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
     }
    }

    stage('Sonar scan'){
            def scannerHome = tool 'Sonar';
            withSonarQubeEnv('Sonar'){
                sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=${student} -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }
    }

    stage('Testing') {
    parallel(
        'pre-integration-test': {
                echo "mvn pre-integration-test"
            },
        'integration-test': {
                withMaven(maven: 'M3') {
                    sh "mvn -f helloworld-project/helloworld-ws/pom.xml integration-test"
                }
            },
        'post-integration-test': {
                echo "mvn post-integration-test"
        }
    )
    }

    stage ('Triggering job and fetching artefact after finishing') {
            build job: 'MNTLAB-shanchar-child1-build-job',
            parameters:[
                string(name: 'BRANCH_NAME',value: "${student}")
            ], wait: true
            copyArtifacts(projectName: 'MNTLAB-shanchar-child1-build-job', target: 'copy')
    }
    

    stage('Packaging and Publishing results') {
    parallel(
        'Archiving artifact': {
                git branch: "${student}", url: 'https://github.com/MNT-Lab/p193e-module.git'
                sh """
                cp Jenkinskfile copy
                cp helloworld-project/helloworld-ws/helloworld-ws.war copy
                tar czf pipeline-${student}-${BUILD_NUMBER}.tar.gz -C copy .
                curl -v -u admin:admin --upload-file pipeline-${student}-${BUILD_NUMBER}.tar.gz http://nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${BUILD_NUMBER}/pipeline-${student}-${BUILD_NUMBER}.tar.gz
                """
        },
        'Creating Docker Image  with naming convention': {
                echo "curl by docker image"
            }
    )
    }
}
