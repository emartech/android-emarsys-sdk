package com.emarsys.geofence

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.api.geofence.Geofence
import com.emarsys.mobileengage.di.mobileEngage

@Mockable
class Geofence(private val loggingInstance: Boolean = false) : GeofenceApi {
    override val registeredGeofences: List<Geofence>
        get() = (if (loggingInstance) mobileEngage().loggingGeofenceInternal else mobileEngage().geofenceInternal)
            .registeredGeofences

    override fun enable() {
        (if (loggingInstance) mobileEngage().loggingGeofenceInternal else mobileEngage().geofenceInternal)
                .enable(null)
    }

    override fun enable(completionListener: CompletionListener) {
        (if (loggingInstance) mobileEngage().loggingGeofenceInternal else mobileEngage().geofenceInternal)
                .enable(completionListener)
    }

    override fun isEnabled(): Boolean {
        return (if (loggingInstance) mobileEngage().loggingGeofenceInternal else mobileEngage().geofenceInternal)
                .isEnabled()
    }

    override fun disable() {
        (if (loggingInstance) mobileEngage().loggingGeofenceInternal else mobileEngage().geofenceInternal)
                .disable()
    }

    override fun setEventHandler(eventHandler: EventHandler) {
        (if (loggingInstance) mobileEngage().loggingGeofenceInternal else mobileEngage().geofenceInternal)
                .setEventHandler(eventHandler)
    }

    override fun setInitialEnterTriggerEnabled(enabled: Boolean) {
        (if (loggingInstance) mobileEngage().loggingGeofenceInternal else mobileEngage().geofenceInternal)
                .setInitialEnterTriggerEnabled(enabled)
    }
}
