package com.emarsys.geofence

import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.geofence.GeofenceInternal

class GeofenceProxy(private val geofenceInternal: GeofenceInternal, private val runnerProxy: RunnerProxy) : GeofenceApi {
    override fun enable() {
        runnerProxy.logException {
            geofenceInternal.enable(null)
        }
    }

    override fun enable(completionListener: CompletionListener) {
        runnerProxy.logException {
            geofenceInternal.enable(completionListener)
        }
    }

    override fun disable() {
    }

    override fun setEventHandler(eventHandler: EventHandler) {
        runnerProxy.logException {
            geofenceInternal.setEventHandler(eventHandler)
        }
    }
}
