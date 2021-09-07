package com.emarsys.mobileengage.geofence

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.geofence.model.Geofence
import com.emarsys.mobileengage.geofence.model.TriggeringEmarsysGeofence

interface GeofenceInternal {

    val registeredGeofences: List<Geofence>

    fun fetchGeofences(completionListener: CompletionListener?)
    fun enable(completionListener: CompletionListener?)
    fun disable()
    fun isEnabled(): Boolean
    fun registerGeofences(geofences: List<Geofence>)
    fun onGeofenceTriggered(triggeringEmarsysGeofences: List<TriggeringEmarsysGeofence>)
    fun setEventHandler(eventHandler: EventHandler)
    fun setInitialEnterTriggerEnabled(enabled: Boolean)
}