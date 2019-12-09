node {
    stage('Preparation (Checking out)') {
        checkout([$class: 'GitSCM', branches: [[name: '*/anikitsenka']],
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
            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=anikitsenka -Dsonar.sources=helloworld-project/helloworld-ws/src -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
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
    stage('Triggering job and fetching artefact after finishing'){
        build job: "MNTLAB-anikitsenka-child1-build-job", parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "anikitsenka"]], wait: true;
        copyArtifacts(filter:'*', projectName: 'MNTLAB-anikitsenka-child1-build-job', selector: lastSuccessful());
        sh 'ls -lha'
    }
    stage('Packaging and Publishing results'){
        parallel (
            arch: {
                sh '''
                    tar -zxf anikitsenka_dsl_script.tar.gz output.txt
                    ls -lha
                    ls -lha helloworld-project/helloworld-ws/target
                    tar -czf pipeline-anikitsenka-${BUILD_NUMBER}.tar.gz output.txt helloworld-project/helloworld-ws/target/helloworld-ws.war
                    ls -lha
                '''
                stash includes: "pipeline-anikitsenka-${BUILD_NUMBER}.tar.gz", name: "artefact_targz"
            },
            dock: {
                sh '''
                    echo "Where is ur Docker image?"
                    '''
                    // docker build -t anikitsenka/tomcat .
                    // docker tag anikitsenka/tomcat 192.168.56.106:30083/anikitsenka/tomcat:${BUILD_ID}
                    // docker login -u admin -p nexus 192.168.56.106:30083
                    // docker push 192.168.56.106:30083/anikitsenka/tomcat:${BUILD_ID}

            }
        )
    }
}
