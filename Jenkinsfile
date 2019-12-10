node {
   try {
    def STUDENT = 'ymatveichyk'
    def mavenName = 'M3'
    def SonarName = 'Sonar'
    def SonarTool = 'Sonar'
	
	
	
    stage('Preparation') {

        checkout scm
//        checkout([$class: 'GitSCM', branches: [[name: "*/$STUDENT"]], userRemoteConfigs: [[url: ' https://github.com/MNT-Lab/p193e-module']]])
    }

	
    stage('Creation metadata page'){
        sh label: '', script: '''builddate=$(date)
        cat << EOF > helloworld-ws/src/main/webapp/metadata.html
        build: $BUILD_NUMBER <br>
        author: ymatveichyk <br>
        build_url: $BUILD_URL <br>
        buils_data: $builddate
        EOF'''

    }


      stage('Building code') {
            echo 'Building code'
            withMaven(maven: "${mavenName}") {
            sh 'mvn -B clean package -f helloworld-project/helloworld-ws/pom.xml'
        }
    }
	

	
    stage('Sonar scan') {
        def scannerHome = tool 'SonarQubeScanner'
        withSonarQubeEnv("${SonarName}") {
            sh "${scannerHome}/bin/sonar-scanner " +
                    "-Dsonar.projectKey=helloworld-ws-$STUDENT " +
                    '-Dsonar.language=java ' +
                    '-Dsonar.sources=helloworld-ws/src ' +
                    '-Dsonar.java.binaries=helloworld-ws/target'
        }
    }
	


    stage('Test') {
            parallel 'mvn pre-integration-test': {
                stage('mvn pre-integration-test') {
                    echo 'mvn pre-integration-test'
                    }
            }, 'mvn integration-test': {
                stage('mvn integration-test') {
                    echo 'mvn integration-test' 
                    withMaven(maven: "${mavenName}") { sh "mvn -B integration-test -f helloworld-project/helloworld-ws/pom.xml" }
                    }
            }, 'mvn post-integration-test': {
                stage('mvn post-integration-test') {
                    echo 'mvn post-integration-test'
                    }
                }

    }



    stage('Triggering job and fetching artefact after finishing'){
        build job: "MNTLAB-${STUDENT}-child1-build-job", parameters: [string(name: 'BRANCH_NAME', value: "${STUDENT}")], wait: true
        copyArtifacts filter: "output.txt", fingerprintArtifacts: true, projectName: "MNTLAB-${STUDENT}-child1-build-job", selector: lastSuccessful()
    }
	




