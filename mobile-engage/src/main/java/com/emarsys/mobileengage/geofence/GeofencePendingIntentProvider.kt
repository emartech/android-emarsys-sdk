package com.emarsys.mobileengage.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.emarsys.core.Mockable

@Mockable
class GeofencePendingIntentProvider(private val context: Context) {
    fun providePendingIntent(): PendingIntent {
        val intent = Intent("com.emarsys.sdk.GEOFENCE_ACTION")

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}