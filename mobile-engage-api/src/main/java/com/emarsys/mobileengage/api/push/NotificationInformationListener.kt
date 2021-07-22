package com.emarsys.mobileengage.api.push

fun interface NotificationInformationListener {
    fun onNotificationInformationReceived(notificationInformation: NotificationInformation)
}