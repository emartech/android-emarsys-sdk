package com.emarsys.service

import com.emarsys.mobileengage.di.mobileEngage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EmarsysMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        mobileEngage().coreSdkHandler.post {
            if (mobileEngage().deviceInfo.isAutomaticPushSendingEnabled) {
                mobileEngage().pushInternal.setPushToken(token, null)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        EmarsysMessagingServiceUtils.handleMessage(this@EmarsysMessagingService, remoteMessage)
    }
}