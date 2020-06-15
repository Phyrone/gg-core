pipeline {
    agent any
    stages {
        stage('Clean') {
            steps {
                sh 'gradle clean'
            }
        }
        stage('Compile') {
            steps {
                sh 'gradle shadowJar'
            }
        }

        stage('Archive') {
            steps {
                parallel('Publish': {
                    withCredentials([usernamePassword(credentialsId: 'admin-nexus-repo-phyrone-de', usernameVariable: 'REPO_USER', passwordVariable: 'REPO_PASSWORD')]) {
                        sh 'gradle publish'
                    }
                }, 'Archive': {
                    archiveArtifacts(artifacts: 'build/libs/*.jar')
                })
            }
        }

    }
    tools {
        jdk 'oraclejdk8'
        gradle 'gradle6'
    }
}