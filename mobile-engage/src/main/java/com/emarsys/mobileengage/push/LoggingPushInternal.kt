package com.emarsys.mobileengage.push

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener

class LoggingPushInternal(private val klass: Class<*>) : PushInternal {
    override fun setPushToken(pushToken: String, completionListener: CompletionListener?) {
        val parameters = mapOf(
            "push_token" to pushToken,
            "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override val pushToken: String?
        get() {
            val callerMethodName = SystemUtils.getCallerMethodName()
            debug(MethodNotAllowed(klass, callerMethodName, null))
            return null
        }

    override fun clearPushToken(completionListener: CompletionListener?) {
        val parameters: Map<String, Any?> = mapOf(
            "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun trackMessageOpen(
        sid: String?,
        completionListener: CompletionListener?
    ) {
        val parameters = mapOf(
            "messageId" to sid,
            "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun setNotificationEventHandler(notificationEventHandler: EventHandler) {
        val parameters: Map<String, Any?> = mapOf(
            "notification_event_handler" to true
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun setSilentMessageEventHandler(silentMessageEventHandler: EventHandler) {
        val parameters: Map<String, Any?> = mapOf(
            "silent_message_event_handler" to true
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun setNotificationInformationListener(notificationInformationListener: NotificationInformationListener) {
        val parameters: Map<String, Any?> = mapOf(
            "notification_information_listener" to true
        )

        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun setSilentNotificationInformationListener(silentNotificationInformationListener: NotificationInformationListener) {
        val parameters: Map<String, Any?> = mapOf(
            "notification_information_listener" to true
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }
}