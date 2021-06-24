package com.emarsys.service

import android.content.Context
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.service.MessagingServiceUtils
import com.huawei.hms.push.RemoteMessage

object EmarsysHuaweiMessagingServiceUtils {
    fun handleMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
        val handler: CoreSdkHandler = mobileEngage().coreSdkHandler

        handler.post {
            MessagingServiceUtils.handleMessage(
                    context,
                    remoteMessage.dataOfMap,
                    mobileEngage().deviceInfo,
                    mobileEngage().notificationCache,
                    mobileEngage().timestampProvider,
                    mobileEngage().fileDownloader,
                    mobileEngage().silentMessageActionCommandFactory,
                    mobileEngage().remoteMessageMapper)
        }

        return isMobileEngageMessage(remoteMessage.dataOfMap)
    }

    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>): Boolean {
        return MessagingServiceUtils.isMobileEngageMessage(remoteMessageData)
    }
}