package com.emarsys.service

import com.emarsys.Emarsys.Push.setPushToken
import com.emarsys.core.api.proxyApi
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.di.Container.getDependency
import com.emarsys.service.EmarsysMessagingServiceUtils.handleMessage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EmarsysMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (getDependency<DeviceInfo>().isAutomaticPushSendingEnabled) {
            setPushToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Runnable { handleMessage(this@EmarsysMessagingService, remoteMessage) }
                .proxyApi(getDependency("coreSdkHandler"))
    }
}