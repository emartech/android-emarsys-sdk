package com.emarsys.push

import android.content.Intent
import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.mobileengage.di.mobileEngage

@Mockable
class Push(private val loggingInstance: Boolean = false) : PushApi {

    override fun trackMessageOpen(intent: Intent) {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .trackMessageOpen(intent, null)
    }

    override fun trackMessageOpen(
            intent: Intent,
            completionListener: CompletionListener) {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .trackMessageOpen(intent, completionListener)
    }

    override fun setPushToken(pushToken: String) {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .setPushToken(pushToken, null)
    }

    override fun setPushToken(
            pushToken: String,
            completionListener: CompletionListener) {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .setPushToken(pushToken, completionListener)
    }

    override fun getPushToken(): String? {
        return (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .pushToken
    }

    override fun setNotificationInformationListener(notificationInformationListener: NotificationInformationListener) {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .setNotificationInformationListener(notificationInformationListener)
    }

    override fun setSilentNotificationInformationListener(silentNotificationInformationListener: NotificationInformationListener) {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .setSilentNotificationInformationListener(silentNotificationInformationListener)
    }

    override fun clearPushToken() {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .clearPushToken(null)
    }

    override fun clearPushToken(completionListener: CompletionListener) {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .clearPushToken(completionListener)
    }

    override fun setNotificationEventHandler(notificationEventHandler: EventHandler) {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .setNotificationEventHandler(notificationEventHandler)
    }

    override fun setSilentMessageEventHandler(silentMessageEventHandler: EventHandler) {
        (if (loggingInstance) mobileEngage().loggingPushInternal else mobileEngage().pushInternal)
                .setSilentMessageEventHandler(silentMessageEventHandler)
    }
}
