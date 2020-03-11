package com.emarsys.mobileengage.geofence

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.geofence.model.Geofence
import com.emarsys.mobileengage.geofence.model.TriggeringGeofence

interface GeofenceInternal {

    fun fetchGeofences()
    fun enable(completionListener: CompletionListener?)
    fun registerGeofences(geofences: List<Geofence>)
    fun onGeofenceTriggered(events: List<TriggeringGeofence>)
}