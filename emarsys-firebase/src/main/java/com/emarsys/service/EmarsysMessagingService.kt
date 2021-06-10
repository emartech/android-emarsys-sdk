package com.emarsys.service

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.di.getDependency
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.mobileengage.push.PushInternal
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EmarsysMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        getDependency<CoreSdkHandler>().post {
            if (getDependency<DeviceInfo>().isAutomaticPushSendingEnabled) {
                getDependency<PushInternal>("defaultInstance").setPushToken(token, null)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        EmarsysMessagingServiceUtils.handleMessage(this@EmarsysMessagingService, remoteMessage)
    }
}