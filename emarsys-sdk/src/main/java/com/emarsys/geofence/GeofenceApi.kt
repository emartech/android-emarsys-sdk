package com.emarsys.geofence

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler

interface GeofenceApi {
    fun enable()
    fun enable(completionListener: CompletionListener)
    fun enable(completionListener: (Throwable?) -> Unit)
    fun disable()
    fun setEventHandler(eventHandler: EventHandler)
}