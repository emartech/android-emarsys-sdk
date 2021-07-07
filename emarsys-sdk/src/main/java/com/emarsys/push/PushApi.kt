package com.emarsys.push

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener

interface PushApi {
    fun setPushToken(
            pushToken: String,
            completionListener: CompletionListener? = null)

    var pushToken: String?
    fun clearPushToken()
    fun clearPushToken(completionListener: CompletionListener)
    fun setNotificationEventHandler(notificationEventHandler: EventHandler)
    fun setSilentMessageEventHandler(silentMessageEventHandler: EventHandler)
    fun setNotificationInformationListener(notificationInformationListener: NotificationInformationListener)
    fun setSilentNotificationInformationListener(silentNotificationInformationListener: NotificationInformationListener)
}