def label = "docker-jenkins-${UUID.randomUUID().toString()}"

node {
    stage('Preparation (Checking out)') {
        checkout([$class: 'GitSCM', branches: [[name: '*/vmarkau']],
        userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/build-t00ls.git']]])
    }
    stage ('Build') {
        withMaven(maven: 'M3'){
            sh 'mvn clean verify -f helloworld-project/helloworld-ws/pom.xml'
            sh 'mvn package -f helloworld-project/helloworld-ws/pom.xml'
        }
    }
    stage('Sonar scan'){
        def scannerHome = tool 'Sonar';
        withSonarQubeEnv(){
            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=vmarkau -Dsonar.sources=helloworld-project/helloworld-ws/src -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
        }
    }
    stage('Testing'){
        parallel (
            test1: {
                withMaven(maven: 'M3'){
                    sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml'
                }
            },
            test2: { sh 'echo "preintegration-test"' },
            test3: { sh 'echo "postintegration-test"' }
        )
    }
    
    
}
