pipeline {
    agent any
    // agent {
    //     docker {
    //         image 'maven:3-alpine'
    //         label 'my-defined-label'
    //         args  '-v /tmp:/tmp'
    //     }
    // }
    environment { 
    studentbranch = 'ekomarov'
    }
    
    stages {
        stage('Preparation') {
            steps { 
                echo 'Preparation'
                github('MNT-Lab/p193e-module', "$studentbranch")
            }
        }
        stage('Building code') {
            steps { 
                echo 'Building code'
            }
        }
        stage('Sonar scan') {
            steps { 
                echo 'Sonar scan'
            }
        }
        stage('Testing') {
            steps { 
                echo 'Testing'
            }
        }
        stage('Triggering job and fetching artefact after finishing') {
            steps { 
                echo 'Triggering job and fetching artefact after finishing'
            }
        }
        stage('Packaging and Publishing results') {
            steps { 
                echo 'Packaging and Publishing results'
            }
        }
        stage('Asking for manual approval') {
            steps { 
                echo 'Asking for manual approval'
            }
        }
        stage('Deployment') {
            steps { 
                echo 'Deployment'
            }
        }
    }
}