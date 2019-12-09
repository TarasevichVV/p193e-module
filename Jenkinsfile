def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def student = "shanchar"
def label2 = "centos-jenkins-${UUID.randomUUID().toString()}"

node {
    
    stage ('Preparation (Checking out)') {
      checkout scm
      sh "echo -e 'BUILD NUMBER: $BUILD_NUMBER\nBuldTime: $(date)\nTriggeredBy: $(git log origin/shanchar -1) \nArtifact Version: 1.$BUILD_NUMBER' \nAuthor=${student} >> index.html"
      sh "mv index.html helloworld-project/helloworld-ws/src/main/webapp/index.html"
    }
    
    stage ('Building code') {
      git branch: "${student}", url: 'https://github.com/MNT-Lab/build-t00ls.git'
      withMaven(maven: 'M3') {
      sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install" 
     }
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
                                       docker build -t nexus-dock.k8s.playpit.by:80/helloworld-${student}:$BUILD_NUMBER .
                                       docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                       docker push nexus-dock.k8s.playpit.by:80/helloworld-${student}:$BUILD_NUMBER
                                       docker rmi nexus-dock.k8s.playpit.by:80/helloworld-${student}:$BUILD_NUMBER
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


    stage ('Deployment (rolling update, zero downtime)') {
        podTemplate(label: label2,
                containers: [
                        containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                        containerTemplate(name: 'centos', image: 'centos', command: 'cat', ttyEnabled: true),
                    ],
                    volumes: [
                        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                    ]
                ) {
            node(label2) {
                    stage('Sab2') {
                        container('centos') {
                            echo "Building docker image..."
                            sh """
                                curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
                                chmod +x ./kubectl
                                mv ./kubectl /usr/local/bin/kubectl
                                kubectl apply -f https://raw.githubusercontent.com/MNT-Lab/p193e-module/shanchar/yaml.yaml
                                kubectl patch deploy shanchar-deploy -n shanchar --patch="{
                                    'spec':{
                                        'template':{
                                            'spec':{
                                                'containers':[{'name':'shanchar-deploy','nexus-dock.k8s.playpit.by:80/helloworld-shanchar:$BUILD_NUMBER'}]
                                                }
                                            }
                                        }
                                    }"
                                sleep 5
                                kubectl get ns
                                kubectl get svc -n shanchar

                                kubectl get pods -n shanchar
                               """
                        }
                    }
            }
        }
    }




}
