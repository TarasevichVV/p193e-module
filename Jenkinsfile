def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def label1 = "centos-jenkins-${UUID.randomUUID().toString()}"
node {
    stage('SCM git checkout') {
        git branch: 'ashkraba', url: 'https://github.com/MNT-Lab/p193e-module.git'
        checkout([$class: 'GitSCM', branches: [[name: '*/ashkraba']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/MNT-Lab/p193e-module.git']]])
    }
    stage('Maven build package') {
        withMaven(maven: 'M3') {
            sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
            sh "cp helloworld-project/helloworld-ws/target/*.war ./helloworld.war"
        }
    }
    /*
    stage('Sonar scan for code') {
        def scannerHome = tool 'Sonar'
        withSonarQubeEnv('Sonar') {
            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ashkraba_helloworld -Dsonar.sources=helloworld-project/helloworld-ws/src -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
        }
    }
    stage('Maven testing') {
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

     */
    stage('Triggering job from prev task') {
        build job: "MNTLAB-ashkraba-child1-build-job", parameters: [string(name: "BRANCH_NAME", value: "ashkraba")], wait: true
        copyArtifacts(projectName: "MNTLAB-ashkraba-child1-build-job", selector: lastSuccessful())
    }
    stage('Create archive and push it to nexus') {
        sh "tar -zxvf ashkraba_dsl_script.tar.gz"
        sh "tar -czf pipeline-ashkraba-\"${BUILD_NUMBER}\".tar.gz output.txt Jenkinsfile helloworld.war Dockerfile"
        sh "curl -v -u admin:admin --upload-file pipeline-ashkraba-\"${BUILD_NUMBER}\".tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/askraba/\"${BUILD_NUMBER}\"/pipeline-ashkraba-\"${BUILD_NUMBER}\".tar.gz"
    }
    stage('Build Docker Image and push to nexus') {
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
                container('docker') {
                    sh """
                        apk add wget"
                        wget --user admin --password admin http://nexus.k8s.playpit.by/repository/maven-releases/app/askraba/\"${BUILD_NUMBER}\"/pipeline-ashkraba-\"${BUILD_NUMBER}\".tar.gz
                        tar -xzvf pipeline-ashkraba-\"${BUILD_NUMBER}\".tar.gz
                        docker build -t helloworld-ashkrba:"${BUILD_NUMBER}" .
                        docker tag helloworld-ashkrba:"${BUILD_NUMBER}" nexus-dock.k8s.playpit.by:80/helloworld-ashkraba:"${BUILD_NUMBER}"
                        docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                        docker push nexus-dock.k8s.playpit.by:80/helloworld-ashkraba:"${BUILD_NUMBER}"
                        docker rmi helloworld-ashkraba:"${BUILD_NUMBER}"
                       """
                }
            }
        }
    }
    stage('Asking for manual approval') {
        stage = env.STAGE_NAME
        timeout(time: 2, unit: "MINUTES") { input message: "Do you want deploy this image", ok: 'Approve' }
    }

    stage ('Deploy my image to k8s') {
        podTemplate(label: label1,
                containers: [
                        containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                        containerTemplate(name: 'centos', image: 'centos', command: 'cat', ttyEnabled: true),
                ],
                volumes: [
                        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                ]
        ) {
            node(label1) {
                    container('centos') {
                        sh """
                           curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
                           chmod +x ./kubectl
                           mv ./kubectl /usr/local/bin/kubectl
                           kubectl apply -f https://raw.githubusercontent.com/MNT-Lab/p193e-module/ashkkraba/my_image_deploy.yaml
                           kubectl set image deployment tomcat -n ashkraba tomcat=nexus-dock.k8s.playpit.by:80/helloworld-ashkraba:"$BUILD_NUMBER"
                           """
                    }
                }
            }
        }
    }


