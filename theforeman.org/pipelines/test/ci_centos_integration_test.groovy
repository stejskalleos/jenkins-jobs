pipeline {
    agent { label 'el' }
    environment {
        cico_job_name = "foreman-ci-centos-simple-test"
    }

    options {
        timestamps()
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
        ansiColor('xterm')
    }

    stages {
        stage('Run simple ci.centos.org test') {
            steps {
                deleteDir()
                git_clone_jenkins_jobs()

                withCredentials([usernamePassword(credentialsId: 'centos-jenkins-openshift', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    runPlaybook(
                        playbook: 'centos.org/ansible/jenkins_job.yml',
                        extraVars: [
                            "jenkins_job_name": "${env.cico_job_name}",
                            "jenkins_job_link_file": "${env.WORKSPACE}/jobs/${cico_job_name}"
                        ],
                        sensitiveExtraVars: ["jenkins_username": "${env.USERNAME}", "jenkins_password": "${env.PASSWORD}"]
                    )
                }

                withCredentials([usernamePassword(credentialsId: 'centos-jenkins-openshift', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    runPlaybook(
                        playbook: 'centos.org/ansible/jenkins_job.yml',
                        extraVars: [
                            "jenkins_job_name": "${env.cico_job_name}",
                            "jenkins_job_link_file": "${env.WORKSPACE}/jobs/${env.cico_job_name}-2"
                        ],
                        sensitiveExtraVars: ["jenkins_username": "${env.USERNAME}", "jenkins_password": "${env.PASSWORD}"]
                    )
                }
            }
            post {
                always {
                    script {
                        set_job_build_description("${env.cico_job_name}", 'first', "${env.WORKSPACE}/jobs/${cico_job_name}")
                        set_job_build_description("${env.cico_job_name}-2", 'second', "${env.WORKSPACE}/jobs/${env.cico_job_name}-2")
                    }
                }
            }
        }
    }
}
