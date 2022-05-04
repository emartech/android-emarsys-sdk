package com.emarsys.service

import android.content.Context
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.service.MessagingServiceUtils
import com.huawei.hms.push.RemoteMessage

object EmarsysHuaweiMessagingServiceUtils {
    
    @JvmStatic
    fun handleMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
        val handlerHolder: ConcurrentHandlerHolder = mobileEngage().concurrentHandlerHolder

        handlerHolder.coreHandler.post {
            MessagingServiceUtils.handleMessage(
                context,
                remoteMessage.dataOfMap,
                mobileEngage().deviceInfo,
                mobileEngage().fileDownloader,
                mobileEngage().silentMessageActionCommandFactory,
                mobileEngage().remoteMessageMapper
            )
        }

        return isMobileEngageMessage(remoteMessage.dataOfMap)
    }

    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>): Boolean {
        return MessagingServiceUtils.isMobileEngageMessage(remoteMessageData)
    }
}