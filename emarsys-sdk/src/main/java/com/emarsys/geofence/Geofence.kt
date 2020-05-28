package com.emarsys.geofence

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.getDependency
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.geofence.GeofenceInternal

@Mockable
class Geofence(private val loggingInstance: Boolean = false) : GeofenceApi {
    override fun enable() {
        (if (loggingInstance) getDependency<GeofenceInternal>("loggingInstance") else getDependency<GeofenceInternal>("defaultInstance"))
                .enable(null)
    }

    override fun enable(completionListener: CompletionListener) {
        (if (loggingInstance) getDependency<GeofenceInternal>("loggingInstance") else getDependency<GeofenceInternal>("defaultInstance"))
                .enable(completionListener)
    }

    override fun isEnabled(): Boolean {
       return (if (loggingInstance) getDependency<GeofenceInternal>("loggingInstance") else getDependency<GeofenceInternal>("defaultInstance"))
                .isEnabled()
    }

    override fun enable(completionListener: (Throwable?) -> Unit) {
        (if (loggingInstance) getDependency<GeofenceInternal>("loggingInstance") else getDependency<GeofenceInternal>("defaultInstance"))
                .enable(CompletionListener { completionListener.invoke(it) })
    }

    override fun disable() {
        (if (loggingInstance) getDependency<GeofenceInternal>("loggingInstance") else getDependency<GeofenceInternal>("defaultInstance"))
                .disable()
    }

    override fun setEventHandler(eventHandler: EventHandler) {
        (if (loggingInstance) getDependency<GeofenceInternal>("loggingInstance") else getDependency<GeofenceInternal>("defaultInstance"))
                .setEventHandler(eventHandler)
    }
}
