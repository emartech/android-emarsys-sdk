package com.emarsys.mobileengage.event

import android.content.Context
import com.emarsys.mobileengage.api.event.EventHandler
import org.json.JSONObject

class CacheableEventHandler : EventHandler {

    private var events: MutableList<Triple<Context, String, JSONObject?>> = mutableListOf()
    var eventHandler: EventHandler? = null
        set(value) {
            if (value != null) {
                events.forEach { event ->
                    value.handleEvent(event.first, event.second, event.third)
                }
                events.clear()
            }
            field = value
        }

    override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
        if (eventHandler == null) {
            events.add(Triple(context, eventName, payload))
        } else {
            eventHandler?.handleEvent(context, eventName, payload)
        }
    }
}