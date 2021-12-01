package com.emarsys.mobileengage.notification.command

import android.content.Context
import android.os.Handler
import com.emarsys.core.Mockable
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.CacheableEventHandler
import org.json.JSONObject

@Mockable
class AppEventCommand(private val context: Context,
                      private val cacheableEventHandler: CacheableEventHandler,
                      private val uiHandler: Handler,
                      val name: String,
                      val payload: JSONObject?) : Runnable {

    val notificationEventHandler: EventHandler?
        get() = cacheableEventHandler

    override fun run() {
        val eventHandler = cacheableEventHandler
        uiHandler.post {
            eventHandler.handleEvent(context, name, payload)
        }
    }
}