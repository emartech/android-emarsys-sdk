package com.emarsys.service

import android.content.Context
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.service.MessagingServiceUtils
import com.emarsys.mobileengage.service.MessagingServiceUtils.handleMessage
import com.emarsys.mobileengage.service.MessagingServiceUtils.isMobileEngageNotification
import com.google.firebase.messaging.RemoteMessage


object EmarsysFirebaseMessagingServiceUtils {
    var MESSAGE_FILTER = MessagingServiceUtils.MESSAGE_FILTER
    var V2_MESSAGE_FILTER = MessagingServiceUtils.V2_MESSAGE_FILTER

    @JvmStatic
    fun handleMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
        val handlerHolder: ConcurrentHandlerHolder = mobileEngage().concurrentHandlerHolder
        val isMobileEngageNotification = isMobileEngageNotification(remoteMessage.data)

        if (isMobileEngageNotification) {
            handlerHolder.coreHandler.post {
                handleMessage(
                    context,
                    remoteMessage.data,
                    mobileEngage().deviceInfo,
                    mobileEngage().fileDownloader,
                    mobileEngage().silentMessageActionCommandFactory,
                    mobileEngage().remoteMessageMapperFactory,
                )
            }
        }

        return isMobileEngageNotification
    }

    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>): Boolean {
        return isMobileEngageNotification(remoteMessageData)
    }
}