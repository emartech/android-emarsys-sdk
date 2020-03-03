package com.emarsys.geofence

import com.emarsys.core.api.result.CompletionListener

interface GeofenceApi {
    fun enable()
    fun enable(completionListener: CompletionListener)
    fun disable()
}