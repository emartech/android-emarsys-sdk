package com.emarsys.mobileengage.api.push

interface NotificationInformationListener {
    fun onNotificationInformationReceived(notificationInformation: NotificationInformation)
}