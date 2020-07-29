package com.emarsys.mobileengage.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle

object IntentUtils {
    @JvmStatic
    fun createLaunchIntent(remoteIntent: Intent, context: Context): Intent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val remoteExtras = remoteIntent.extras
        if (remoteExtras != null && intent != null) {
            intent.putExtras(remoteIntent.extras!!)
        }
        return intent
    }

    @JvmStatic
    fun createNotificationHandlerServiceIntent(
            context: Context, remoteMessageData: Map<String, String?>,
            notificationId: Int,
            action: String?): Intent {
        val intent = Intent(context, NotificationHandlerService::class.java)
        if (action != null) {
            intent.action = action
        }
        val bundle = Bundle()
        val keys = remoteMessageData.keys
        for (key in keys) {
            bundle.putString(key, remoteMessageData[key])
        }

        bundle.putInt("notification_id", notificationId)
        intent.putExtra("payload", bundle)
        return intent
    }

    fun createNotificationHandlerServicePendingIntent(
            context: Context,
            remoteMessageData: Map<String, String?>,
            notificationId: Int): PendingIntent {
        return createNotificationHandlerServicePendingIntent(context, remoteMessageData, notificationId, null)
    }

    @JvmStatic
    fun createNotificationHandlerServicePendingIntent(
            context: Context,
            remoteMessageData: Map<String, String?>,
            notificationId: Int,
            action: String?): PendingIntent {
        val intent = createNotificationHandlerServiceIntent(context, remoteMessageData, notificationId, action)
        return PendingIntent.getService(
                context,
                (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                intent,
                0)
    }
}