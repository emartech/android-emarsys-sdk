package com.emarsys.push

import android.content.Intent
import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.getDependency
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.push.PushInternal

@Mockable
class Push(private val loggingInstance: Boolean = false) : PushApi {

    override fun trackMessageOpen(intent: Intent) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PushInternal>("defaultInstance"))
                .trackMessageOpen(intent, null)
    }

    override fun trackMessageOpen(
            intent: Intent,
            completionListener: CompletionListener) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PushInternal>("defaultInstance"))
                .trackMessageOpen(intent, completionListener)
    }

    override fun setPushToken(pushToken: String) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PushInternal>("defaultInstance"))
                .setPushToken(pushToken, null)
    }

    override fun setPushToken(
            pushToken: String,
            completionListener: CompletionListener) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PushInternal>("defaultInstance"))
                .setPushToken(pushToken, completionListener)
    }

    override fun clearPushToken() {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PushInternal>("defaultInstance"))
                .clearPushToken(null)
    }

    override fun clearPushToken(completionListener: CompletionListener) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PushInternal>("defaultInstance"))
                .clearPushToken(completionListener)
    }

    override fun setNotificationEventHandler(notificationEventHandler: EventHandler) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PushInternal>("defaultInstance"))
                .setNotificationEventHandler(notificationEventHandler)
    }

    override fun setSilentMessageEventHandler(silentMesssageEventHandler: EventHandler) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PushInternal>("defaultInstance"))
                .setSilentMessageEventHandler(silentMesssageEventHandler)
    }
}
