package com.emarsys.mobileengage.geofence

import com.emarsys.core.api.result.CompletionListener

interface GeofenceInternal {

    fun fetchGeofences()
    fun enable(completionListener: CompletionListener?)
}