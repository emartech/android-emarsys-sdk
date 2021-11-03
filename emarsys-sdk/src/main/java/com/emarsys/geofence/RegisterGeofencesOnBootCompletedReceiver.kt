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
            Log.d(Logger.TAG, "Emarsys SDK has been started!")
        }
    }
}