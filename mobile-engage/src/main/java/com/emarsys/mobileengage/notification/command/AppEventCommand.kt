package com.emarsys.mobileengage.notification.command

import android.content.Context
import android.os.Handler
import com.emarsys.core.Mockable
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.EventHandlerProvider
import org.json.JSONObject

@Mockable
class AppEventCommand(private val context: Context,
                      private val eventHandlerProvider: EventHandlerProvider,
                      private val uiHandler: Handler,
                      val name: String,
                      val payload: JSONObject?) : Runnable {

    val notificationEventHandler: EventHandler?
        get() = eventHandlerProvider.eventHandler

    override fun run() {
        val eventHandler = eventHandlerProvider.eventHandler
        uiHandler.post {
            eventHandler?.handleEvent(context, name, payload)
        }
    }
}