package com.emarsys.sample

import android.app.Application
import android.content.Context
import android.widget.Toast
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

        Emarsys.setup(config)
    }

    override fun handleEvent(eventName: String?, payload: JSONObject?) {
        Toast.makeText(this, eventName + " - " + payload.toString(), Toast.LENGTH_LONG).show()
    }

    override fun handleEvent(context: Context?, eventName: String?, payload: JSONObject?) {
        Toast.makeText(this, eventName + " - " + payload.toString(), Toast.LENGTH_LONG).show()
    }
}