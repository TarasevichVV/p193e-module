#!/usr/bin/env groovy
def student = "ykachatkou"
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def label2 = "centos-jenkins-${UUID.randomUUID().toString()}"
def nexus = "nexus-dock.k8s.playpit.by:80"
node {
    try {
        stage('Preparation (Checking out)') {
            checkout scm
        }
        stage('Build') {
            stage = env.STAGE_NAME
            git branch: "ykachatkou", url: "https://github.com/MNT-Lab/build-t00ls"
            def index = '''
                <p>commitId: $(git log -n 1 --pretty=format:%H)</p>
                <p>triggeredBy: $(git show -s --pretty=%an)</p>
                <p>buildtime: $(date +'%Y-%m-%d_%H-%M-%S')</p>
                <p>version: 1.$BUILD_NUMBER</p>
            '''
            sh """
              echo "${index}" > helloworld-project/helloworld-ws/src/main/webapp/index.html
            """

            withMaven(maven: "M3") {
                sh "mvn -f helloworld-project/helloworld-ws/pom.xml clean install"
            }
        }

        stage('Sonar scanner') {
            def scannerHome = tool name: 'Sonar'
            withSonarQubeEnv(installationName: 'Sonar') {
                sh "${scannerHome}/bin/sonar-scanner  -e -Dsonar.projectKey=${student} -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }

        }
        stage('Testing') {
            stage = env.STAGE_NAME
            parallel(
                    'pre-integration-test': {
                        echo "mvn pre-integration-test"
                    },
                    'integration-test': {
                        withMaven(maven: "M3") {
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
                        tar xzf ${student}_dsl_script.tar.gz
                        cp /var/jenkins_home/workspace/EPBYMINW9138/mntlab-ci-pipeline@script/* .
                        cp helloworld-project/helloworld-ws/target/helloworld-ws.war .
                        tar czf pipeline-${student}-${BUILD_NUMBER}.tar.gz helloworld-ws.war output.txt Jenkinsfile 
                        curl -v -u admin:admin --upload-file pipeline-${student}-${
                            BUILD_NUMBER
                        }.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${BUILD_NUMBER}/pipeline-${student}-${BUILD_NUMBER}.tar.gz
                        """
                        stash includes: "helloworld-ws.war", name: "targz"
                        stash includes: "Dockerfile", name: "docker"
                        stash includes: "*.yml", name: "yml"
                    },
                    'Creating Docker Image ': {
                        podTemplate(label: label,
                                containers: [
                                        containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                                        containerTemplate(name: 'docker', image: 'docker:18-dind', command: 'cat', ttyEnabled: true),
                                ],
                                volumes: [
                                        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')

                                ])

                                {
                                    node(label) {
                                        container('docker') {
                                            unstash "targz"
                                            unstash "docker"
                                            sh """
                                            docker build -t ${nexus}/helloworld-ykachatkou:$BUILD_NUMBER .
                                            docker login -u admin -p admin ${nexus}
                                            docker push ${nexus}/helloworld-ykachatkou:$BUILD_NUMBER
                                            docker rmi ${nexus}/helloworld-ykachatkou:$BUILD_NUMBER
                                            """
                                        }

                                    }
                                }


                    }

            )
        }
        stage("Asking for manual approval") {
            stage = env.STAGE_NAME
            timeout(time: 10, unit: "MINUTES") {
                input message: 'Approve Deploy?', ok: 'Yes'

            }


        }
        stage('Deployment (rolling update, zero downtime)') {
            stage = env.STAGE_NAME
            podTemplate(label: label2,
                    containers: [
                            containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                            containerTemplate(name: 'centos', image: 'centos', ttyEnabled: true),
                    ]
            )

                    {
                        node(label2) {

                            container('centos') {
                                unstash "yml"
                                sh """
                                curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
                                chmod +x ./kubectl
                                mv ./kubectl /usr/local/bin/kubectl
                                sed -i "s|helloworld-ykachatkou|helloworld-ykachatkou:$BUILD_NUMBER|" docker-deploy.yml
                                kubectl apply -f docker-deploy.yml
                                """

                            }

                        }
                    }


        }
        currentBuild.result = 'SUCCESS'
    }
    catch (err) {
        currentBuild.result = 'FAILURE'
    }
    finally {
        stage('Sending status'){
            def mailRecipients = "yury.kachatkou@gmail.com"
            def jobName = currentBuild.fullDisplayName

            emailext body: '''${SCRIPT, template="groovy-html.template"}''',
            mimeType: 'text/html',
            subject: "$currentBuild.result: Job '${env.JOB_NAME} ${env.BUILD_NUMBER}' Stage:'${stage}'",
            to: "${mailRecipients}",
            replyTo: "${mailRecipients}",
            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
     }
  }
    
}
