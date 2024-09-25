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
import com.emarsys.sample.dashboard.button.trackPushToken
import com.emarsys.sample.pref.Prefs
import org.json.JSONObject

class SampleApplication : Application(), EventHandler, NotificationInformationListener {
    private val logTag = "SampleApplication"

    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        val context = this.applicationContext

        val config = EmarsysConfig(
            application = this,
            applicationCode = Prefs.applicationCode.ifEmpty { null },
            merchantId = Prefs.merchantId,
            verboseConsoleLoggingEnabled = true
        )

        Emarsys.setup(config)
        Prefs.loggedIn = false
        Prefs.hardwareId = Emarsys.config.hardwareId
        Prefs.languageCode = Emarsys.config.languageCode
        Prefs.sdkVersion = Emarsys.config.sdkVersion
        createNotificationChannels()
        setupEventHandlers()
        setContactIfPresent(context)
    }

    private fun setContactIfPresent(context: Context) {
        if (Prefs.contactFieldId != 0 && Prefs.contactFieldValue.isNotEmpty()) {
            Emarsys.setContact(
                contactFieldValue = Prefs.contactFieldValue,
                contactFieldId = Prefs.contactFieldId
            ) {
                if (it == null) {
                    trackPushToken(context = context)
                }
            }
            Prefs.loggedIn = true
        }
    }

    private fun setupEventHandlers() {
        if (Prefs.applicationCode.isNotEmpty()) {
            Emarsys.push.setNotificationEventHandler(this)
            Emarsys.push.setSilentMessageEventHandler(this)
            Emarsys.push.setNotificationInformationListener(this)
            Emarsys.push.setSilentNotificationInformationListener(this)
            Emarsys.inApp.setEventHandler(this)
            Emarsys.geofence.setEventHandler(this)
            Emarsys.onEventAction.setOnEventActionEventHandler(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        id: String,
        name: String,
        description: String,
        importance: Int
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(id, name, importance)
        notificationChannel.description = description
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel(
                "ems_sample_news",
                "News",
                "News and updates go into this channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            createNotificationChannel(
                "ems_sample_messages",
                "Messages",
                "Important messages go into this channel",
                NotificationManager.IMPORTANCE_HIGH
            )
        }
    }

    override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
        if (eventName != "push:payload") {
            Toast.makeText(this.applicationContext, "$eventName - $payload", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onNotificationInformationReceived(notificationInformation: NotificationInformation) {
        Log.d(logTag, "campaignId: " + notificationInformation.campaignId)
    }
}