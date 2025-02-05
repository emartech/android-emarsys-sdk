package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.event.EventServiceInternal

class TrackActionClickCommand(
    private val eventServiceInternal: EventServiceInternal,
    private val buttonId: String,
    val sid: String
) : Runnable {
    override fun run() {
        val payload: MutableMap<String, String> = HashMap()
        payload["button_id"] = buttonId
        payload["origin"] = "button"
        payload["sid"] = sid

        eventServiceInternal.trackInternalCustomEventAsync("push:click", payload, null)
    }
}
