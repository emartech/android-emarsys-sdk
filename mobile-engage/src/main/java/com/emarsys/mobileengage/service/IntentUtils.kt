package com.emarsys.mobileengage.service

import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import com.emarsys.core.util.AndroidVersionUtils.isUpsideDownCakeOrHigher
import com.emarsys.mobileengage.di.mobileEngage

object IntentUtils {
    @JvmStatic
    fun createLaunchPendingIntent(remoteIntent: Intent, context: Context): PendingIntent? {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val remoteExtras = remoteIntent.extras
        if (remoteExtras != null && launchIntent != null) {
            launchIntent.putExtras(remoteIntent.extras!!)
        }

        val activityOptions = if (isUpsideDownCakeOrHigher) {
            ActivityOptions.makeBasic()
                .setPendingIntentCreatorBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle()
        } else null

        return if (launchIntent != null) PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
            activityOptions
        ) else null
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
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}