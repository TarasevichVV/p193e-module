#!/usr/bin/env groovy
def student = "ykachatkou"
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def Dockerfile = """
                       From alpine
                       
                       RUN apk update && apk add wget tar openjdk8 && \
                       wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.20/bin/apache-tomcat-8.5.20.tar.gz && \
                       tar -xvf apache-tomcat-8.5.20.tar.gz && \
                       mkdir /opt/tomcat && \
                       mv apache-tomcat*/* /opt/tomcat/
                       COPY helloworld-ws.war /opt/tomcat/webapps/
                       EXPOSE 8080
                       CMD ["/opt/tomcat/bin/catalina.sh", "run"]
                       
"""

node {
    stage('Preparation (Checking out)') {
        git branch: 'ykachatkou', credentialsId: '6d32c01d-6f1a-4a3f-9402-f9a133852eb2', url: 'https://github.com/MNT-Lab/build-t00ls'
    }
    stage('Build') {
        sh '''
      cat << EOF > helloworld-project/helloworld-ws/src/main/webapp/index.html
      <p>commitId: $GIT_COMMIT </p>
      <p>triggeredBy: $(git show -s --pretty=%an)</p>
      <p>buildtime: $(date +'%Y-%m-%d_%H-%M-%S')</p>
      <p>version: 1. $BUILD_NUMBER</p>
      EOF
      '''
        withMaven(maven: "Maven-3.6.3") {
            sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
        }
    }

    stage('Sonar scanner') {
        def scannerHome = tool name: 'sonarscanner1'
        //def scannerHome = tool 'sonarscanner1'
        withSonarQubeEnv(installationName: 'sonar') {
            sh "${scannerHome}/bin/sonar-scanner  -e -Dsonar.projectKey=${student}-maven -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
        }

    }
    stage('Testing') {
        parallel(
                'pre-integration-test': {
                    echo "mvn pre-integration-test"
                },
                'integration-test': {
                    withMaven(maven: "Maven-3.6.3") {
                        sh "mvn -f helloworld-project/helloworld-ws/pom.xml integration-test"
                    }
                },
                'post-integration-test': {
                    echo "mvn post-integration-test"
                }

        )
    }
    stage('Triggering job and fetching artefact after finishing') {
        build job: "MNTLAB-${student}-child1-build-job", parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]], wait: true
        copyArtifacts projectName: "MNTLAB-${student}-child1-build-job", selector: lastCompleted()
    }
    stage('Packaging and Publishing results') {
        parallel(
                'Archiving artifact': {
                    sh """
                touch Jenkinsfile
                tar xzf ${student}_dsl_script.tar.gz
                cp helloworld-project/helloworld-ws/target/helloworld-ws.war .
                tar czf pipeline-${student}-${BUILD_NUMBER}.tar.gz helloworld-ws.war output.txt Jenkinsfile
                curl -v -u admin:123_qwe --upload-file pipeline-${student}-${
                        BUILD_NUMBER
                    }.tar.gz http://nexus-svc.nexus.svc.cluster.local/repository/maven-releases/app/${student}/${
                        BUILD_NUMBER
                    }/pipeline-${student}-${BUILD_NUMBER}.tar.gz
                """
                },
                'Creating Docker Image ': {
                    podTemplate(label: label,
                            containers: [
                                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                                    containerTemplate(name: 'docker', image: 'docker:18-dind', command: 'cat', ttyEnabled: true),
                            ],
                            volumes: [
                                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                                    hostPathVolume(hostPath: '/data/workspace/pipe', mountPath: '/home')

                            ])

                            {
                                node(label) {
                                    stage('Docker Build') {
                                        container('docker') {
                                            echo "Building docker image..."
                                            sh """
                        cp /home/helloworld-project/helloworld-ws/target/helloworld-ws.war .
                        echo "192.168.56.240    nexus3" >> /etc/hosts
                        cat /etc/hosts
                        echo "${Dockerfile}" > Dockerfile
                        docker build -t nexus3:8111/helloworld-ykachatkou:rc-$BUILD_NUMBER .
                        docker login -u admin -p 123_qwe nexus3:8111
                        docker push nexus3:8111/helloworld-ykachatkou:rc-$BUILD_NUMBER
                        """
                                        }
                                    }
                                }
                            }


                }

        )
    }
    stage("hello") {
        sh """
        hostname
        whoami
        cat /etc/passwd
        touch superfile
        """

    }
}
