package com.emarsys.sample

import com.emarsys.Emarsys
import com.emarsys.service.EmarsysFirebaseMessagingServiceUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CustomMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Emarsys.push.setPushToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val handledByEmarsysSDK =
                EmarsysFirebaseMessagingServiceUtils.handleMessage(this, remoteMessage)

        if (!handledByEmarsysSDK) {
            //handle your custom push message here
        }
    }
}