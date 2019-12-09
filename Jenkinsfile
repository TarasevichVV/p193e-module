node {
    stage('SCM Checkout') {
        git branch: 'ashkraba', url: 'https://github.com/MNT-Lab/p193e-module.git'
        checkout([$class: 'GitSCM', branches: [[name: '*/ashkraba']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/p193e-module.git']]])
    }
    stage('Mvn Package') {
        withMaven(maven: 'M3') {
            sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
            sh "cp helloworld-project/helloworld-ws/target/*.war ./helloworld.war"
        }
    }
    stage('Sonar scan') {
        def scannerHome = tool 'Sonar'
        withSonarQubeEnv('Sonar') {
            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ashkraba_helloworld -Dsonar.sources=helloworld-project/helloworld-ws/src -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
        }
    }
    stage('Testing') {
        parallel(
                'pre-integration': {
                    sh 'echo "mvn pre-integration-test"'
                },
                'integration-test': {
                    withMaven(maven: 'M3') {
                        sh "mvn -f helloworld-project/helloworld-ws/pom.xml integration-test"
                    }
                },
                'post-integration-test': {
                    sh 'echo "mvn post-integration-test"'
                }
        )
    }
    stage('Triggering job') {
        build job: "MNTLAB-ashkraba-child1-build-job", parameters: [string(name: "BRANCH_NAME", value: "ashkraba")], wait: true
        copyArtifacts(projectName: "MNTLAB-ashkraba-child1-build-job", selector: lastSuccessful())
    }
    stage('Create archive') {
        sh "tar -zxvf ashkraba_dsl_script.tar.gz"
        sh "tar -czf pipeline-ashkraba-\"${BUILD_NUMBER}\".tar.gz output.txt Jenkinsfile helloworld.war"
    }
}
