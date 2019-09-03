package com.emarsys.sample

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.emarsys.Emarsys
import com.emarsys.config.EmarsysConfig
import com.emarsys.mobileengage.api.EventHandler
import com.emarsys.mobileengage.api.NotificationEventHandler
import org.json.JSONObject

public class SampleApplication : Application(), EventHandler, NotificationEventHandler {
    override fun onCreate() {
        super.onCreate()
        val config = EmarsysConfig.Builder()
                .application(this)
                .mobileEngageApplicationCode("EMS11-C3FD3")
                .contactFieldId(3)
                .predictMerchantId("1428C8EE286EC34B")
                .inAppEventHandler(this)
                .notificationEventHandler(this)
                .build()

        createNotificationChannels()

        Emarsys.setup(config)
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

    override fun handleEvent(eventName: String?, payload: JSONObject?) {
        Toast.makeText(this, eventName + " - " + payload.toString(), Toast.LENGTH_LONG).show()
    }

    override fun handleEvent(context: Context?, eventName: String?, payload: JSONObject?) {
        Toast.makeText(this, eventName + " - " + payload.toString(), Toast.LENGTH_LONG).show()
    }
}