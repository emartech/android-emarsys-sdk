package com.emarsys.mobileengage.geofence

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.geofence.model.Geofence
import com.emarsys.mobileengage.geofence.model.TriggeringGeofence

interface GeofenceInternal {

    fun fetchGeofences(completionListener: CompletionListener?)
    fun enable(completionListener: CompletionListener?)
    fun disable()
    fun isEnabled(): Boolean
    fun registerGeofences(geofences: List<Geofence>)
    fun onGeofenceTriggered(events: List<TriggeringGeofence>)
    fun setEventHandler(eventHandler: EventHandler)
}