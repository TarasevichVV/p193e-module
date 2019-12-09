def student = apavlovsky
def branch_name = apavlovsky

node {
    stage('Preparation (Checking out)'){
        git branch: "${branch_name}",
            url: 'https://github.com/MNT-Lab/build-t00ls.git'
    }

    stage('Building'){
        sh 'echo "Stage building"'
    }

    stage('Sonar scan'){
        sh 'echo "Stage sonar scan"'
    }

    stage('Testing'){
        sh 'echo "Stage testing"'
    }

    stage('Triggering job and fetching artefact'){
        sh 'echo "Stage triggering job"'
    }

    stage('Packaging and Publishing results'){
        sh 'echo "Packaging and Publishing"'
    }

    stage('Asking for manual approval'){
        sh 'echo "Manual approval"'
    }

    stage('Deployment'){
        sh 'echo "Deployment"'
    }

}
