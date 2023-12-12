package com.emarsys.mobileengage.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.emarsys.mobileengage.di.mobileEngage

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
        context: Context,
        notificationData: NotificationData,
        action: String?
    ): Intent {
        val intent = Intent(context, mobileEngage().notificationOpenedActivityClass)
        if (action != null) {
            intent.action = action
        }

        intent.putExtra("payload", notificationData)
        return intent
    }

    fun createNotificationHandlerServicePendingIntent(
        context: Context,
        notificationData: NotificationData
    ): PendingIntent {
        return createNotificationHandlerServicePendingIntent(context, notificationData, null)
    }

    @JvmStatic
    fun createNotificationHandlerServicePendingIntent(
        context: Context,
        notificationData: NotificationData,
        action: String?
    ): PendingIntent {
        val intent = createNotificationHandlerServiceIntent(context, notificationData, action)
        return PendingIntent.getActivity(
                context,
                (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
}