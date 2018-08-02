@Library(['general-pipeline']) _

node('master'){
    withSlack channel:'jenkins',{
        timeout(15){
            stage('core'){
                build job: 'android-core-sdk'
            }
            stage('mobile-engage'){
                build job: 'android-mobile-engage-sdk'
            }
            stage('sample'){
                build job: 'android-emarsys-sdk-sample'
            }
        }
    }
}