package com.emarsys.service

import android.content.Context
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.service.MessagingServiceUtils
import com.emarsys.mobileengage.service.MessagingServiceUtils.isMobileEngageNotification
import com.huawei.hms.push.RemoteMessage

object EmarsysHuaweiMessagingServiceUtils {
    private const val MESSAGE_FILTER = "ems_msg"
    private const val V2_MESSAGE_FILTER = "ems.version"

    @JvmStatic
    fun handleMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
        val handlerHolder: ConcurrentHandlerHolder = mobileEngage().concurrentHandlerHolder
        val isMobileEngageNotification = isMobileEngageNotification(remoteMessage.dataOfMap)

        if (isMobileEngageNotification) {
            handlerHolder.coreHandler.post {
                MessagingServiceUtils.handleMessage(
                    context,
                    remoteMessage.dataOfMap,
                    mobileEngage().deviceInfo,
                    mobileEngage().fileDownloader,
                    mobileEngage().silentMessageActionCommandFactory,
                    mobileEngage().remoteMessageMapperFactory
                )
            }
        }

        return isMobileEngageNotification
    }

    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>): Boolean {
        return isMobileEngageNotification(remoteMessageData)
    }
}