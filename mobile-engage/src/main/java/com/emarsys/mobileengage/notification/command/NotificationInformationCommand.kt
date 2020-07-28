package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.push.NotificationInformationListenerProvider

class NotificationInformationCommand(private val notificationInformationListenerProvider: NotificationInformationListenerProvider,
                                     val notificationInformation: NotificationInformation) : Runnable {

    override fun run() {
        notificationInformationListenerProvider.notificationInformationListener?.onNotificationInformationReceived(notificationInformation)
    }
}