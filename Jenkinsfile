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
            archiveArtifacts(artifacts: 'build/libs/*.jar')
        }
    }
    tools {
        jdk 'oraclejdk8'
        gradle 'gradle6'
    }
}