package com.emarsys.mobileengage.geofence

import com.emarsys.core.api.result.CompletionListener

class LoggingGeofenceInternal(private val klass: Class<*>): GeofenceInternal {

    override fun fetchGeofences() {
    }

    override fun enable(completionListener: CompletionListener?) {
    }
}