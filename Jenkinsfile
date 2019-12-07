//#parse("File Header.java")
node {
    stage('Checkout') {
        checkout scm
        echo "checkout from dev branch"

        }

    stage('Build') {
        withMaven(maven: "M3") { //maven3-6-3
        sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
        }
    }
    stage('Sonar'){
        environment {
            scannerHome = tool 'Sonar'
        }
        withSonarQubeEnv('Sonar') {
            sh "${scannerHome}/bin/sonar-scanner"
        }
        timeout(time: 10, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: true

    }
    }
    stage('Test') {
        parallel(
            preintegration: {
                sh 'echo \'mvn pre-integration-test\''},
            integrationtest: {
                sh 'echo \'This is integration-test\''
                sh 'mvn integration-test -f helloworld-ws/pom.xml'
            },
            postintegration: {
                sh "echo \'mvn post-integration-test\'"}
        )
    }
    stage('Triggering') {

        sh "echo trigering"
    }
    stage('Packeging') {

        sh "echo Packeging"

    }
    stage('Asking approval') {

       sh "echo Asking"

    }
    stage('Deploy') {

        sh "echo deploy"

    }
    stage('Sending status') {

        sh "echo Sending status"

    }
}
