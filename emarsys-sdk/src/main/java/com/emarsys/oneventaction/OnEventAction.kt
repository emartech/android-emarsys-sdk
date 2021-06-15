package com.emarsys.oneventaction

import android.content.Context

import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.event.EventHandlerProvider
import org.json.JSONObject

class OnEventAction : OnEventActionApi {

    override fun setOnEventActionEventHandler(eventHandler: EventHandler) {
        val onEventActionEventHandlerProvider: EventHandlerProvider = mobileEngage().onEventActionEventHandlerProvider
        onEventActionEventHandlerProvider.eventHandler = eventHandler
    }

    override fun setOnEventActionEventHandler(eventHandler: (context: Context, eventName: String, payload: JSONObject?) -> Unit) {
        val onEventActionEventHandlerProvider: EventHandlerProvider = mobileEngage().onEventActionEventHandlerProvider
        onEventActionEventHandlerProvider.eventHandler =  object : EventHandler {
            override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                eventHandler.invoke(context, eventName, payload)
            }
        }
    }
}