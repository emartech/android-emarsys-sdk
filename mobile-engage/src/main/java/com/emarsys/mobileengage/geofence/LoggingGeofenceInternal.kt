package com.emarsys.mobileengage.geofence

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.geofence.model.Geofence
import com.emarsys.mobileengage.geofence.model.TriggeringGeofence

class LoggingGeofenceInternal(private val klass: Class<*>): GeofenceInternal {

    override fun fetchGeofences() {
    }

    override fun enable(completionListener: CompletionListener?) {
    }

    override fun registerGeofences(geofences: List<Geofence>) {
    }

    override fun onGeofenceTriggered(events: List<TriggeringGeofence>) {
    }
}