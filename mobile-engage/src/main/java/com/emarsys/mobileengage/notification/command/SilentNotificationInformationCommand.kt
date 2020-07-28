package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.push.SilentNotificationInformationListenerProvider

class SilentNotificationInformationCommand(
        private val silentNotificationInformationListenerProvider: SilentNotificationInformationListenerProvider,
        private val notificationInformation: NotificationInformation): Runnable {

    override fun run() {
        silentNotificationInformationListenerProvider.silentNotificationInformationListener?.onNotificationInformationReceived(notificationInformation)
    }
}