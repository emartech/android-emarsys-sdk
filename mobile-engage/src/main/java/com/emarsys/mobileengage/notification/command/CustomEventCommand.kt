package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.event.EventServiceInternal

class CustomEventCommand(
    private val eventServiceInternal: EventServiceInternal,
    val eventName: String,
    val eventAttributes: Map<String, String>?,
    private val triggerTimestamp: Long? = null
) : Runnable {

    override fun run() {
        if (triggerTimestamp != null) {
            eventServiceInternal.trackCustomEventAsync(eventName, eventAttributes, null, triggerTimestamp)
        } else {
            eventServiceInternal.trackCustomEventAsync(eventName, eventAttributes, null)
        }
    }
}
