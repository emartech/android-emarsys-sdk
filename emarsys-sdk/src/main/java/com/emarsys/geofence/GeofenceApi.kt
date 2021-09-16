package com.emarsys.geofence

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.geofence.Geofence

interface GeofenceApi {
    val registeredGeofences: List<Geofence>

    fun enable()
    fun enable(completionListener: CompletionListener)
    fun disable()
    fun setEventHandler(eventHandler: EventHandler)
    fun isEnabled(): Boolean
    fun setInitialEnterTriggerEnabled(enabled: Boolean)
}