@Library(['general-pipeline']) _

node('master') {
    withSlack channel: 'jenkins', {
        timeout(45) {
            stage('init') {
                deleteDir()
                git url: 'git@github.com:emartech/android-emarsys-sdk.git', branch: 'master'
            }

            lock(env.ANDROID_EMARSYS_SDK_BUILD) {
                stage('core') {
                    build job: 'android-core-sdk'
                }

                stage('mobile-engage') {
                    build job: 'android-mobile-engage-sdk'
                }

                stage('predict') {
                    build job: 'android-predict-sdk'
                }

                stage('emarsys') {
                    build job: 'android-emarsys-sdk'
                }

                stage('sample') {
                    build job: 'android-emarsys-sdk-sample'
                }

                def version = sh(script: 'git describe', returnStdout: true).trim()
                def statusCode = sh returnStdout: true, script: "curl -I https://jcenter.bintray.com/com/emarsys/emarsys-sdk/$version/ | head -n 1 | cut -d ' ' -f2".trim()
                def releaseExists = "200" == statusCode.trim()
                if (version ==~ /\d+\.\d+\.\d+/ && !releaseExists) {
                    stage('release-bintray') {
                        slackMessage channel: 'jenkins', text: "Releasing Emarsys SDK $version."
                        sh './gradlew clean build -x lint -x test release'
                        slackMessage channel: 'jenkins', text: "Emarsys SDK $version released to Bintray."
                    }
                }
            }
        }
    }
}
