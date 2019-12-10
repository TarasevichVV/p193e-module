#!/usr/bin/env groovy

def student = 'apavlovsky'
def label = "docker-jenkins-${UUID.randomUUID().toString()}"

node {
    stage('Preparation (Checking out)'){
        checkout scm
    }

    stage('Building') {
        git branch: 'apavlovsky', url: 'https://github.com/MNT-Lab/build-t00ls.git'
        def index_html = """
        <html>
        <head><title>helloworld-ws Quickstart</title></head>
        <body>
        <h1>My customized change to helloworld-ws</h1>
        <p>The <i>helloworld-ws</i> quickstart demonstrates the use of <b>JAX-WS</b> in
            Red Hat JBoss Enterprise Application Platform as a simple Hello World application</p>
        <p>There is no user interface for this quickstart. Instead, you can verify the
             Web Service is running and deployed correctly by accessing the following URL:</p>
             <div style="margin-left: 1em;">
             <a href="HelloWorldService?wsdl">HelloWorldService?wsdl</a>
             </div>
             <p>This URL will display the WSDL definition for the deployed Web Service endpoint.</p>
            <p><h1>Status values:</h1></p>
            <p><b>BuildTime:</b> \$(date)</p>
            <p><b>TriggeredBy:</b> by SCM, every 10 minutes </p>
            <p><b>ArtifactBuild:</b> 1.0.$BUILD_NUMBER </p>
            <p><b>BuildName:</b> $BUILD_DISPLAY_NAME </p>
            </body>
        </html>
        """

        sh 'echo "${index_html} > helloworld-project/helloworld-ws/src/main/webapp/index.html"'

        withMaven(maven: 'M3') {
            sh "mvn clean install -U -f helloworld-project/helloworld-ws/pom.xml"
        }
    }
/*
    stage('Sonar scan'){
        def scannerHome = tool name: 'Sonar'
        withSonarQubeEnv(installationName: 'Sonar') {
            sh "${scannerHome}/bin/sonar-scanner  -e -Dsonar.projectKey=${student} -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
        }
    }
*/
    stage('Testing'){
        parallel(
                1: {
                    echo "Maven pre-integration tests"
                },
                2: {
                    withMaven(maven: 'M3') {
                        sh "mvn -f helloworld-project/helloworld-ws/pom.xml integration-test"
                    }
                },
                3: {
                    echo "Maven pre-integration tests"
                }
        )
    }

    stage('Triggering job and fetching artefact'){
        build job: "MNTLAB-${student}-child1-build-job", parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]], wait: true
        copyArtifacts projectName: "MNTLAB-${student}-child1-build-job", selector: lastCompleted()
    }

    stage('Packaging and Publishing results'){
        parallel(
                'Archive': {
                    stage("Archiving artifact form MNTLAB-${student}-child1-build-job") {
                        sh """
                        tar -xzvf ${student}_dsl_script.tar.gz; 
                        cp helloworld-project/helloworld-ws/target/helloworld-ws.war .
                        tar -czvf pipeline-${student}-${BUILD_NUMBER}.tar.gz output.txt helloworld-ws.war
                        curl -v -u admin:admin --upload-file pipeline-${student}-${BUILD_NUMBER}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${BUILD_NUMBER}/pipeline-${student}-${BUILD_NUMBER}.tar.gz
                        """
                        stash includes: 'helloworld-project/helloworld-ws/target/helloworld-ws.war', name: 'war'
                    }

                },
                'Docker': {
                    podTemplate(label: label,
                            containers: [
                                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                                    containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
                            ],
                            volumes: [
                                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                            ]
                    )
                    node(label) {
                        container('docker') {
                            sh """
                            cat << "EOF" > Dockerfile
                            FROM tomcat:8.0
                            COPY helloworld-ws.war /usr/local/tomcat/webapps/
                            """
                            unstash "war"
                            sh """
                            docker build -t helloworld-${student}:"${BUILD_NUMBER}" .
                            docker tag helloworld-${student}:"${BUILD_NUMBER}" nexus-dock.k8s.playpit.by:80/helloworld-${student}:"${BUILD_NUMBER}"
                            docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                            docker push nexus-dock.k8s.playpit.by:80/helloworld-${student}:"${BUILD_NUMBER}"
                          """
                        }
                    }

                }

        )

    }

    stage('Asking for manual approval'){
        timeout(time: 5, unit: "MINUTES") {
            input message: 'Continue deploy?', ok: 'Yes'
        }
    }

    stage('Deployment'){
        sh 'echo "Deployment"'
    }

}
