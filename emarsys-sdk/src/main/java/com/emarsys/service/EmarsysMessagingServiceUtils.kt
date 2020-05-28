package com.emarsys.service

import android.content.Context
import android.os.Handler
import com.emarsys.core.api.proxyApi
import com.emarsys.core.di.getDependency
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
        val handler: Handler = getDependency("coreSdkHandler")
        var result = false

        handler.post {
            result = handleMessage(
                    context,
                    remoteMessage,
                    getDependency(),
                    getDependency(),
                    getDependency<TimestampProvider>(),
                    getDependency(),
                    getDependency("silentMessageActionCommandFactory"))
            latch.countDown()
        }
        latch.await()

        return result
    }

    fun isMobileEngageMessage(remoteMessageData: Map<String, String?>?): Boolean {
        return MessagingServiceUtils.isMobileEngageMessage(remoteMessageData)
    }
}