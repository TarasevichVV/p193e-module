#!groovy
def student_build ='ekomarov'
def nexusclusterurl = 'nexus-service.nexus.svc.cluster.local:8081'//'nexus.k8s.playpit.by:80'
def nexusurl = 'nexus:80'//'nexus.k8s.playpit.by:80'
def nexusproto = 'http://'
def nexusport = "8123" // '50001'
def nexusmavenrepo = 'maven-releases' //'docker'
def nexusdockerrepo = 'docker-releases' //'docker'
def nexusdockercred = 'admin:admin'
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def Dockerfiletemplate = '''  From alpine

                    RUN apk update && apk add wget tar openjdk8 && \
                        wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.20/bin/apache-tomcat-8.5.20.tar.gz && \
                        tar -xvf apache-tomcat-8.5.20.tar.gz && \
                        mkdir /opt/tomcat && \
                        mv apache-tomcat*/* /opt/tomcat/
                                
                        COPY helloworld-project/helloworld-ws/target/helloworld-ws.war /opt/tomcat/webapps
                                
                        EXPOSE 8080
                        CMD ["/opt/tomcat/bin/catalina.sh", "run"]'''
node('master') {

    stage('Preparation') {
        echo 'Preparation'
        echo "Branch is ${student_build}"
        git branch: "${student_build}", url: 'https://github.com/MNT-Lab/build-t00ls.git'
    }

    stage('Building code') {
        echo 'Building code'
        withMaven(maven: 'Maven') {
        sh 'mvn -B clean package -f helloworld-project/helloworld-ws/pom.xml'
        }
    }

    stage('Sonar scan') {
        echo 'Sonar scan'
        def ST = tool 'Scanner-of-K8s-Sonar';
        withSonarQubeEnv('K8s-sonar') {
            sh "${ST}/bin/sonar-scanner -Dsonar.projectKey=ekomarov_task-11:helloworld-ws -Dsonar.java.binaries=helloworld-project/helloworld-ws/target -Dsonar.sources=helloworld-project/helloworld-ws/src"
        }
    }
    stage('Testing') {
        echo 'Testing'
        parallel 'mvn pre-integration-test': {
            stage('mvn pre-integration-test') {
                echo 'mvn pre-integration-test'
                }
        }, 'mvn integration-test': {
            stage('mvn integration-test') {
                echo 'mvn integration-test' 
                withMaven(maven: 'Maven') { sh "mvn -B integration-test -f helloworld-project/helloworld-ws/pom.xml" }
                }
        }, 'mvn post-integration-test': {
            stage('mvn post-integration-test') {
                echo 'mvn post-integration-test'
                }
        }
    }
    stage('Triggering job and fetching artefact after finishing') {
        echo 'Triggering job and fetching artefact after finishing'
        build job: "MNTLAB-${student_build}-child1-build-job", parameters: [[$class: 'StringParameterValue', name: "BRANCH_NAME", value: "$student_build"]]
        copyArtifacts fingerprintArtifacts: true, projectName: "MNTLAB-${student_build}-child1-build-job", selector: lastSuccessful()
    }
    stage('Packaging and Publishing results') {
        echo 'Packaging and Publishing results'
        parallel "Archiving artifact from MNTLAB-${student_build}-child1-build-job": {
            stage("Archiving artifact from MNTLAB-${student_build}-child1-build-job") {
                echo "Archiving artifact from MNTLAB-${student_build}-child1-build-job"
                sh """
                    tar -xzf ${student_build}_dsl_script.tar.gz; 
                    #ls -ahl;
                    cp helloworld-project/helloworld-ws/target/helloworld-ws.war .
                    tar -czf pipeline-${student_build}-${BUILD_NUMBER}.tar.gz output.txt helloworld-ws.war
                    curl -v -u ${nexusdockercred} --upload-file pipeline-${student_build}-${BUILD_NUMBER}.tar.gz ${nexusproto}${nexusclusterurl}/repository/${nexusmavenrepo}/app/${student_build}/${BUILD_NUMBER}/pipeline-${student_build}-${BUILD_NUMBER}.tar.gz
                    """
                //nexusArtifactUploader artifacts: [[artifactId: "pipeline-${student_build}-${BUILD_NUMBER}.tar.gz", classifier: '', file: "pipeline-${student_build}-${BUILD_NUMBER}.tar.gz", type: '.tar.gz']], credentialsId: '', groupId: 'pipeline.groupId', nexusUrl: "${nexusurl}", nexusVersion: 'nexus3', protocol: "${nexusproto}", repository: "${nexusmavenrepo}", version: "${BUILD_NUMBER}"
                stash includes: 'helloworld-project/helloworld-ws/target/helloworld-ws.war', name: 'target_war'
                }
        }, 'Creating Docker Image': {
            stage('Creating Docker Image') {
                echo 'Creating Docker Image'
                // podTemplate(label: label,
                //     containers: [
                //         containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                //         containerTemplate(name: 'docker', image: 'docker:18-dind', command: 'cat', ttyEnabled: true),
                //     ],
                //     volumes: [
                //         hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')
                //     ]){
                //         node(label) {
                //             stage('Docker Build') {
                //                 container('docker') {
                //                 unstash "target_war"
                //                 sh 'ls'
                //                 sh """
                //                 echo "${Dockerfiletemplate}" > Dockerfile
                //                 docker version
                //                 // #docker build -t helloworld-${student_build}:${BUILD_NUMBER} .
                //                 // #docker tag ${student_build}/app:latest ${nexusurl}/${student_build}/app:${BUILD_NUMBER}
                //                 // #docker login -u admin -p admin ${nexusurl}
                //                 // #docker push ${nexusurl}/${student_build}/app:${BUILD_NUMBER}
                //                 """
                //                 }
                //             }
                //         }
                //     }
            }
        }
    }
    stage('Asking for manual approval') {
        echo 'Asking for manual approval'
        stage = env.STAGE_NAME
        timeout(time: 1, unit: "MINUTES") { input message: "Please, approve deploy tomcat app, build number ${BUILD_NUMBER}", ok: 'Approve' }
    }
    stage('Deployment') {
        echo 'Deployment'
    }
}