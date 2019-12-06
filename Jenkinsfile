node {
    stage('check src to build') {
        git([url: 'https://github.com/MNT-Lab/build-t00ls.git', branch: 'amiasnikovich', credentialsId: '33c48519f78014a6f656a10b73d153cfa1da8f1e'])
    }
    stage('build artefact') {
        withMaven(maven: 'M3') {
            sh 'mvn clean install -f clear_project/helloworld-ws/pom.xml'
        }
    }
    stage('sonar scaner') {
        scannerHome = tool 'Sonar'
        withSonarQubeEnv('Sonar') {
            sh "${scannerHome}/bin/sonar-scanner  -e -Dsonar.projectKey=mias -e -Dsonar.projectName=Hello-world -e -Dsonar.sources=clear_project/helloworld-ws/src -e -Dsonar.java.binaries=clear_project/helloworld-ws/target"
        }
    }
    stage('parallel testing') {
        step {
            parallel (
                    'kind-pre-integration-test': {
                        echo 'mvn pre-integration-test -f clear_project/helloworld-ws/pom.xml'
                    },
                    'integration-test': {
                        withSonarQubeEnv('Sonar') {
                            'mvn integration-test -f clear_project/helloworld-ws/pom.xml'
                        }
                    },
                    'kind-post-integration-test' : {
                        'mvn post-integration-test -f clear_project/helloworld-ws/pom.xml'
                    }
            )
        }
    }
}

