#!/usr/bin/env groovy

def student = 'apavlovsky'

node {
    stage('Preparation (Checking out)'){
        git branch: 'apavlovsky', 
            url: 'https://github.com/MNT-Lab/build-t00ls.git'
    }

    stage('Building') {
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
        <p><b>CommitID</b>: $GIT_COMMIT </p>
        <p><b>BuildTime:</b> \$(date)</p>
        <p><b>TriggeredBy:</b> by SCM, every 10 minutes </p>
        <p><b>ArtifactBuild:</b> 1.0.$BUILD_NUMBER </p>
        <p><b>BuildName:</b> $BUILD_DISPLAY_NAME </p>
        </body>
    </html>
    """
        sh "echo ${index_html} > $WORKSPACE/helloworld-project/helloworld-ws/src/main/webapp/index.html"
        withMaven(maven: 'maven-3', mavenSettingsConfig: 'my-maven-settings') {
            sh "mvn clean install -U -f $WORKSPACE/helloworld-project/helloworld-ws/pom.xml"
        }
    }

    stage('Sonar scan'){
        sh 'echo "Stage sonar scan"'
    }

    stage('Testing'){
        sh 'echo "Stage testing"'
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
