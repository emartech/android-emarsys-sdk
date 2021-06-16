package com.emarsys.service

import android.content.Context
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.util.Assert
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.service.MessagingServiceUtils
import com.emarsys.mobileengage.service.MessagingServiceUtils.handleMessage
import com.google.firebase.messaging.RemoteMessage

object EmarsysMessagingServiceUtils {
    var MESSAGE_FILTER = MessagingServiceUtils.MESSAGE_FILTER

    @JvmStatic
    fun handleMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
        Assert.notNull(context, "Context must not be null!")
        Assert.notNull(remoteMessage, "RemoteMessage must not be null!")

        val handler: CoreSdkHandler = mobileEngage().coreSdkHandler

        handler.post {
            handleMessage(
                    context,
                    remoteMessage,
                    mobileEngage().deviceInfo,
                    mobileEngage().notificationCache,
                    mobileEngage().timestampProvider,
                    mobileEngage().fileDownloader,
                    mobileEngage().silentMessageActionCommandFactory,
                    mobileEngage().remoteMessageMapper)
        }

        return isMobileEngageMessage(remoteMessage.data)
    }

    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>?): Boolean {
        return MessagingServiceUtils.isMobileEngageMessage(remoteMessageData)
    }
}