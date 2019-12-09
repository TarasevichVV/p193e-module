def student="vtarasevich"
def label = "docker-jenkins-${UUID.randomUUID().toString()}"
def labeldeploy = "centos-jenkins-${UUID.randomUUID().toString()}"
node {
    try{
        stage('Preparation (Checking out)'){
        checkout scm
        }
        stage('Building code'){
            git branch: 'vtarasevich', url: 'https://github.com/MNT-Lab/build-t00ls'
            sh label: '', script: '''TimeStamp=$(date)
                                    cat << EOF > helloworld-project/helloworld-ws/src/main/webapp/index.html
                                    <!DOCTYPE html>
                                    <html>
                                    <body>
                                    <h1>author=vtarasevich</h1>
                                    <p>commitId: $(git log -n 1 --pretty=format:%H)</p>
                                    <p>triggeredBy: $(git show -s --pretty=%an)</p>
                                    <p>build_number=${BUILD_NUMBER}</p>
                                    <p>buildTime=$TimeStamp</p>
                                    </body>
                                    </html>
                                    EOF'''
            withMaven(maven: 'M3') { 
                sh "mvn clean -f helloworld-project/helloworld-ws/pom.xml  install"
                stash includes: "helloworld-project/helloworld-ws/target/helloworld-ws.war", name: "war"
                stash includes: "tomcat.yaml", name: "deploy"
                stash includes: "Dockerfile", name: "Dockerfile"
            }
        }
        stage('Sonar scan'){
            def scannerHome = tool 'Sonar';
            withSonarQubeEnv('Sonar'){
                sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=vtarasevich -e -Dsonar.sources=helloworld-project/helloworld-ws/src -e -Dsonar.java.binaries=helloworld-project/helloworld-ws/target"
            }
        }
        stage('Testing') {
            parallel(
            a: {
                sh 'echo "mvn pre-integration-test"'
            },
            b: {
                withMaven(maven: 'M3') { 
                sh 'mvn integration-test -f helloworld-project/helloworld-ws/pom.xml  install'
                }
            },
            c: {
                sh 'echo "mvn post-integration-test"'  
            })
        }
        stage('Triggering job and fetching artefact after finishing'){
            build job: "MNTLAB-${student}-child1-build-job", parameters: [[$class: 'StringParameterValue', name: 'BRANCH_NAME', value: "${student}"]], wait: true
            copyArtifacts(projectName: "MNTLAB-${student}-child1-build-job");
        }
        stage('Packaging and Publishing results'){
            parallel(
                'Creating tar gz': {
                    sh "tar xvzf  ${student}_dsl_script.tar.gz"
                    sh "tar cvzf pipeline-${student}-${currentBuild.number}.tar.gz output.txt Jenkinsfile helloworld-project/helloworld-ws/target/helloworld-ws.war"
                    sh "curl -v -u admin:admin --upload-file pipeline-${student}-${currentBuild.number}.tar.gz nexus.k8s.playpit.by/repository/maven-releases/app/${student}/${currentBuild.number}/pipeline-${student}-${currentBuild.number}.tar.gz"
                    },
                'Creating Docker Image':  {
                    podTemplate(label: label,
                        containers: [
                            containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                            containerTemplate(name: 'docker', image: 'docker:18-dind', command: 'cat', ttyEnabled: true),
                        ],
                        volumes: [
                            hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')
                        ]){
                            node(label) {
                                stage('Docker Build') {
                                    container('docker') {
                                    unstash "war"
                                    unstash "Dockerfile"
                                    sh 'ls'
                                    sh """
                                    docker build -t vtarasevich/app .
                                    docker tag vtarasevich/app:latest nexus-dock.k8s.playpit.by:80/vtarasevich/app:${currentBuild.number}
                                    docker login -u admin -p admin nexus-dock.k8s.playpit.by:80
                                    docker push nexus-dock.k8s.playpit.by:80/vtarasevich/app:${currentBuild.number}
                                    """
                                    }
                                }
                            }
                        }
                    }
                )
            }
        stage("Asking for manual approval") {
            stage = env.STAGE_NAME
            timeout(time: 5, unit: "MINUTES") {
                input message: 'Please, approve deploy tomcat', ok: 'Approve'
            }
        }
        stage('Deployment (rolling update, zero downtime)') {
            stage = env.STAGE_NAME
                podTemplate(label: labeldeploy,
                    containers: [
                        containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                        containerTemplate(name: 'centos', image: 'centos', ttyEnabled: true)
                    ]
                )
                {
                    node(labeldeploy) {
                        container('centos') {
                            unstash "deploy"
                            sh """
                            curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
                            chmod +x ./kubectl
                            mv ./kubectl /usr/local/bin/kubectl
                            sed -i "s%IMAGE%nexus-dock.k8s.playpit.by:80/vtarasevich/app:${currentBuild.number}%" tomcat.yaml
                            kubectl apply -f tomcat.yaml
                            """
                        }
                    }
                }
            }
        currentBuild.result = 'SUCCESS'
    }
    catch (Exception err) {
        currentBuild.result = 'FAILURE'
    }
    finally {
        stage('Sending email'){
            emailext body: '''${SCRIPT, template="groovy-html.template"}''',
            mimeType: 'text/html',
            subject: "$currentBuild.result: Job '$currentBuild.fullDisplayName ${env.BUILD_NUMBER}' Stage:'${stage}'",
            to: "pahosit@gmail.com",
            replyTo: "${mailRecipients}",
            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
        }
    }  
}
