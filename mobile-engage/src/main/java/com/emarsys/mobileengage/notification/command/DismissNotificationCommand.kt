package com.emarsys.mobileengage.notification.command

import android.app.NotificationManager
import android.content.Context
import com.emarsys.mobileengage.service.NotificationData

class DismissNotificationCommand(
    private val context: Context,
    private val notificationData: NotificationData?
): Runnable {

    override fun run() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationData?.collapseId?.let {
            manager.cancel(it, it.hashCode())
        }
    }
}