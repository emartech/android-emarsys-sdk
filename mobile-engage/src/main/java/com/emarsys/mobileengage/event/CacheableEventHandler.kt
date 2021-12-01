package com.emarsys.mobileengage.event

import android.content.Context
import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.Mockable
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.mobileengage.api.event.EventHandler
import org.json.JSONObject

@Mockable
class CacheableEventHandler : EventHandler {

    private var events: MutableList<Triple<Context, String, JSONObject?>> = mutableListOf()
    private var eventHandler: EventHandler? = null
    fun setEventHandler(newEventHandler: EventHandler?) {
        if (newEventHandler != null) {
            events.forEach { event ->
                newEventHandler.handleEvent(event.first, event.second, event.third)
            }
            events.clear()
        }
        eventHandler = newEventHandler
    }

    override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
        if (FeatureRegistry.isFeatureEnabled(InnerFeature.APP_EVENT_CACHE) && eventHandler == null) {
            events.add(Triple(context, eventName, payload))
        } else {
            eventHandler?.handleEvent(context, eventName, payload)
        }
    }
}