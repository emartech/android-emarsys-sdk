package com.emarsys.service

import android.content.Context
import com.emarsys.core.di.getDependency
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.Assert
import com.emarsys.mobileengage.service.MessagingServiceUtils
import com.emarsys.mobileengage.service.MessagingServiceUtils.handleMessage
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.CountDownLatch

object EmarsysMessagingServiceUtils {
    var MESSAGE_FILTER = MessagingServiceUtils.MESSAGE_FILTER

    @JvmStatic
    fun handleMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
        Assert.notNull(context, "Context must not be null!")
        Assert.notNull(remoteMessage, "RemoteMessage must not be null!")

        val latch = CountDownLatch(1)
        val handler: CoreSdkHandler = getDependency()
        var result = false

        handler.post {
            result = handleMessage(
                    context,
                    remoteMessage,
                    getDependency(),
                    getDependency(),
                    getDependency<TimestampProvider>(),
                    getDependency(),
                    getDependency("silentMessageActionCommandFactory"),
                    getDependency())
            latch.countDown()
        }
        latch.await()

        return result
    }

    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>?): Boolean {
        return MessagingServiceUtils.isMobileEngageMessage(remoteMessageData)
    }
}