package com.emarsys.mobileengage.notification.command

import android.app.NotificationManager
import android.content.Context
import android.content.Intent

class DismissNotificationCommand(
    private val context: Context,
    private val intent: Intent): Runnable {

    override fun run() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val bundle = intent.getBundleExtra("payload")
        if (bundle != null) {
            bundle.getString("notification_id")?.let {
                manager.cancel(it, it.hashCode())
            }
        }
    }
}