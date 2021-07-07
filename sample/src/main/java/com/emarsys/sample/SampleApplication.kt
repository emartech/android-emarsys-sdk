package com.emarsys.sample

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.chibatching.kotpref.Kotpref
import com.emarsys.Emarsys
import com.emarsys.config.EmarsysConfig
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.push.NotificationInformation
import com.emarsys.mobileengage.api.push.NotificationInformationListener
import com.emarsys.sample.prefs.Cache
import org.json.JSONObject


open class SampleApplication : Application(), EventHandler, NotificationInformationListener {
    companion object {
        const val TAG = "SampleApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        val config = EmarsysConfig.Builder()
                .application(this)
                .applicationCode(getApplicationCode())
                .contactFieldId(Cache.contactFieldId)
                .merchantId(Cache.merchantId)
                .build()

        createNotificationChannels()
        Emarsys.setup(config)

        if (getApplicationCode() != null) {
            setupEventHandlers()
        }
    }

    fun setupEventHandlers() {
        Emarsys.inApp.setEventHandler(this)
        Emarsys.push.setNotificationEventHandler(this)
        Emarsys.push.setSilentMessageEventHandler(this)
        Emarsys.push.setNotificationInformationListener(this)
        Emarsys.push.setSilentNotificationInformationListener(this)
        Emarsys.geofence.setEventHandler(this)
        Emarsys.onEventAction.setOnEventActionEventHandler(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel("ems_sample_news", "News", "News and updates go into this channel", NotificationManager.IMPORTANCE_HIGH)
            createNotificationChannel("ems_sample_messages", "Messages", "Important messages go into this channel", NotificationManager.IMPORTANCE_HIGH)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String, name: String, description: String, importance: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        notificationManager.createNotificationChannel(channel)
    }

    override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
        if (eventName != "push:payload") {
            Toast.makeText(this, eventName + " - " + payload.toString(), Toast.LENGTH_LONG).show()
        }
    }

    override fun onNotificationInformationReceived(notificationInformation: NotificationInformation) {
        Log.d(TAG, "campaignId: " + notificationInformation.campaignId)
    }

    private fun getApplicationCode(): String? {
        return if (Cache.applicationCode == "not set") {
            null
        } else {
            Cache.applicationCode
        }
    }
}