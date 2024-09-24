package com.emarsys.mobileengage.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.emarsys.core.Mockable
import com.emarsys.core.util.AndroidVersionUtils

@Mockable
class GeofencePendingIntentProvider(private val context: Context) {
    fun providePendingIntent(): PendingIntent {
        val intent = Intent("com.emarsys.sdk.GEOFENCE_ACTION")
        intent.setPackage(context.packageName)
        return if (AndroidVersionUtils.isSOrAbove) {
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}