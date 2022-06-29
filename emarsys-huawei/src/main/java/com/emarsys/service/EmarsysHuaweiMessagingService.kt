package com.emarsys.service

import com.emarsys.mobileengage.di.mobileEngage
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class EmarsysHuaweiMessagingService : HmsMessageService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (mobileEngage().deviceInfo.platform.lowercase() == "android-huawei") {
            mobileEngage().concurrentHandlerHolder.coreHandler.post {
                if (mobileEngage().deviceInfo.isAutomaticPushSendingEnabled) {
                    mobileEngage().pushInternal.setPushToken(token, null)
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (mobileEngage().deviceInfo.platform.lowercase() == "android-huawei") {
            EmarsysHuaweiMessagingServiceUtils.handleMessage(
                this@EmarsysHuaweiMessagingService,
                remoteMessage
            )
        }
    }
}