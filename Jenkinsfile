#!/usr/bin/env groovy

def student = 'apavlovsky'

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

    stage('Sonar scan'){
        def sonarhome = tool name: 'Sonar'
        withSonarQubeEnv(installationName: 'Sonar') {
            sh "${sonarhome}/bin/sonar-scanner  -e -Dsonar.projectKey=${student} -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
        }
    }

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
        sh 'echo "Stage triggering job"'
    }

    stage('Packaging and Publishing results'){
        sh 'echo "Packaging and Publishing"'
    }

    stage('Asking for manual approval'){
        sh 'echo "Manual approval"'
    }

    stage('Deployment'){
        sh 'echo "Deployment"'
    }

}
