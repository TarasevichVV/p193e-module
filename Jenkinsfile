
node ('master') {
    def student = "shanchar"

    stage ('Preparation (Checking out)') {
      checkout scm
    }
    
    stage ('Building code') {
      git branch: "${student}", url: 'https://github.com/MNT-Lab/build-t00ls.git'
      withMaven(maven: 'M3') {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
     }
    }

    stage('Sonar scan'){
            def scannerHome = tool 'Sonar';
            withSonarQubeEnv('mysonar'){
                sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=${student} -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }
    }

    stage('Testing') {
    parallel(
        'pre-integration-test': {
                echo "mvn pre-integration-test"
            },
        'integration-test': {
                withMaven(maven: 'by_pom') {
                    sh "mvn -f helloworld-project/helloworld-ws/pom.xml integration-test"
                }
            },
        'post-integration-test': {
                echo "mvn post-integration-test"
        }
    )
    }

    stage ('Triggering job and fetching artefact after finishing') {
            build job: 'MNTLAB-shanchar-child1-build-job',
            parameters:[
                string(name: 'BRANCH_NAME',value: "${student}")
            ], wait: true
            copyArtifacts(projectName: 'MNTLAB-shanchar-child1-build-job', target: 'copy')
    }
    

    stage('Packaging and Publishing results') {
    parallel(
        'Archiving artifact': {
                sh """
                git branch: "${student}", url: 'https://github.com/MNT-Lab/p193e-module.git'
                cp Jenkinskfile copy
                cp helloworld-project/helloworld-ws/helloworld-ws.war copy
                tar czf pipeline-${student}-${BUILD_NUMBER}.tar.gz -C copy .
                curl -v -u admin:admin --upload-file pipeline-${student}-${BUILD_NUMBER}.tar.gz http://nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${BUILD_NUMBER}/pipeline-${student}-${BUILD_NUMBER}.tar.gz
                """
        },
        'Creating Docker Image  with naming convention': {
                echo "curl by docker image"
            }
    )
    }
}
