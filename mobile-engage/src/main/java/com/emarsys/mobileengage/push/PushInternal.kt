package com.emarsys.mobileengage.push

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformationListener

interface PushInternal {
    fun setPushToken(pushToken: String, completionListener: CompletionListener?)
    val pushToken: String?
    fun clearPushToken(completionListener: CompletionListener?)
    fun trackMessageOpen(sid: String?, completionListener: CompletionListener?)
    fun setNotificationEventHandler(notificationEventHandler: EventHandler)
    fun setSilentMessageEventHandler(silentMessageEventHandler: EventHandler)
    fun setNotificationInformationListener(notificationInformationListener: NotificationInformationListener)
    fun setSilentNotificationInformationListener(silentNotificationInformationListener: NotificationInformationListener)
}