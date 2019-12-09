def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def student = "shanchar"


node ('master') {
    
    stage ('Preparation (Checking out)') {
      checkout scm
    }
    
    stage ('Building code') {
      git branch: "${student}", url: 'https://github.com/MNT-Lab/build-t00ls.git'
      withMaven(maven: 'M3') {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install" 
     }
      sh "ls -l helloworld-project/helloworld-ws/target/"  
      sh "pwd"
      sh "cp helloworld-project/helloworld-ws/target/helloworld-ws.war ."
    }

    stage('Sonar scan'){
            def scannerHome = tool 'Sonar';
            withSonarQubeEnv('Sonar'){
                sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=${student} -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }
    }

    stage('Testing') {
    parallel(
        'pre-integration-test': {
                echo "mvn pre-integration-test"
            },
        'integration-test': {
                withMaven(maven: 'M3') {
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
                git branch: "${student}", url: 'https://github.com/MNT-Lab/p193e-module.git'
                sh """
                cp Jenkinsfile copy
                cp helloworld-ws.war copy
                tar czf pipeline-${student}-${BUILD_NUMBER}.tar.gz -C copy .
                curl -v -u admin:admin --upload-file pipeline-${student}-${BUILD_NUMBER}.tar.gz http://nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${BUILD_NUMBER}/pipeline-${student}-${BUILD_NUMBER}.tar.gz
                """
                stash name: "Dockerfile", includes: "Dockerfile"
                stash name: "warka", includes: "helloworld-ws.war" 
        },
        'Creating Docker Image  with naming convention': {
                echo "curl by docker image"

                podTemplate(label: label,
                        containers: [
                                containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                                containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
                            ],
                            volumes: [
                                hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                            ]
                        ) {
                    node(label) {
                            stage('Sab') {
                                container('docker') {
                                    echo "Building docker image..."
                                    unstash "Dockerfile"
                                    unstash "warka"
                                    sh """
                                       docker build -t nexus-dock.k8s.playpit.by:80/${student}:$BUILD_NUMBER .
                                       docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                       docker push nexus-dock.k8s.playpit.by:80/${student}:$BUILD_NUMBER
                                       """
                                }
                            }
                    }
                }
                
            }
    )
    }

    stage ('Asking for manual approval'){
        script{
            timeout(time: 1, unit: "MINUTES") {
            input "Continue?"
            echo "Jump to next stage"}
    }
    }





}

