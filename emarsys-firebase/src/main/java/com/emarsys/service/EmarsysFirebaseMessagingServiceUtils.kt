package com.emarsys.service

import android.content.Context
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.service.MessagingServiceUtils
import com.emarsys.mobileengage.service.MessagingServiceUtils.handleMessage
import com.google.firebase.messaging.RemoteMessage

object EmarsysFirebaseMessagingServiceUtils {
    var MESSAGE_FILTER = MessagingServiceUtils.MESSAGE_FILTER

    @JvmStatic
    fun handleMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
        val handlerHolder: ConcurrentHandlerHolder = mobileEngage().concurrentHandlerHolder

        handlerHolder.coreHandler.post {
            handleMessage(
                context,
                remoteMessage.data,
                mobileEngage().deviceInfo,
                mobileEngage().fileDownloader,
                mobileEngage().silentMessageActionCommandFactory,
                mobileEngage().remoteMessageMapper
            )
        }

        return isMobileEngageMessage(remoteMessage.data)
    }

    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>): Boolean {
        return MessagingServiceUtils.isMobileEngageMessage(remoteMessageData)
    }
}