package com.emarsys.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.emarsys.Emarsys
import com.emarsys.core.util.log.Logger
import com.emarsys.mobileengage.di.MobileEngageComponent

class RegisterGeofencesOnBootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, true)) {
            if (MobileEngageComponent.instance != null) {
                if (Emarsys.geofence.isEnabled()) {
                    Log.d(Logger.TAG, "Geofences registered")
                } else {
                    Log.d(Logger.TAG, "Geofence feature is not enabled")
                }
            } else {
                Log.d(Logger.TAG, "Unsuccessful setup of Emarsys SDK")
            }
        }
    }
}