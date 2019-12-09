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
            sh "${scannerHome}/bin/sonar-scanner  -e -Dsonar.projectKey=mias -e -Dsonar.projectName=amiasnikovich -e -Dsonar.sources=clear_project/helloworld-ws/src -e -Dsonar.java.binaries=clear_project/helloworld-ws/target"
        }
    }
    stage('parallel testing') {

        parallel(
                'kind-pre-integration-test': {
                    echo 'mvn pre-integration-test -f clear_project/helloworld-ws/pom.xml'
                },
                'integration-test': {
                    withSonarQubeEnv('Sonar') {
                        'mvn integration-test -f clear_project/helloworld-ws/pom.xml'
                    }
                },
                'kind-post-integration-test': {
                    'mvn post-integration-test -f clear_project/helloworld-ws/pom.xml'
                }
        )
    }
    stage('build_child_job') {
        build job: 'DSL-jobs/MNTLAB-amiasnikovich-child1-build-job', parameters: [
                string(name: 'BRANCH_NAME', value: 'amiasnikovich')
        ], wait: true

        copyArtifacts filter: '*.tar.gz', fingerprintArtifacts: true, projectName: 'DSL-jobs/MNTLAB-amiasnikovich-child1-build-job', target: 'Artifact'
    }
    stage('pack_and_pub_res') {
        parallel(
                get_artifact: {
                    sh "tar -xvzf Artifact/amiasnikovich-script.tar.gz"
                    sh "-czvf pipeline-amiasnikovich-${BUILD_NUMBER}.tar.gz -C helloworld-project/helloworld-ws/target/helloworld-ws.war -C Artifact/output.txt" //-C /var/jenkins_home/workspace/EPBYMINW9149/mntlab-ci-pipeline@script/Jenkinsfile"
                }
        )
    }
}
