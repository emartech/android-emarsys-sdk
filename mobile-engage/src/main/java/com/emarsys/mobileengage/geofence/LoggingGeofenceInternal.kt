package com.emarsys.mobileengage.geofence

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.geofence.model.Geofence
import com.emarsys.mobileengage.geofence.model.TriggeringEmarsysGeofence

class LoggingGeofenceInternal(private val klass: Class<*>) : GeofenceInternal {
    override val registeredGeofences: List<Geofence>
        get() = listOf()

    override fun fetchGeofences(completionListener: CompletionListener?) {
        val parameters = mapOf(
                "completionListener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun enable(completionListener: CompletionListener?) {
        val parameters = mapOf(
                "completionListener" to (completionListener != null)
        )

        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun disable() {
        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, null))
    }

    override fun isEnabled(): Boolean {
        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, null))
        return false
    }

    override fun registerGeofences(geofences: List<Geofence>) {
        val parameters = mapOf(
                "completionListener" to geofences
        )

        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun onGeofenceTriggered(triggeringEmarsysGeofences: List<TriggeringEmarsysGeofence>) {
        val parameters = mapOf(
                "triggeringEmarsysGeofences" to triggeringEmarsysGeofences
        )

        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun setEventHandler(eventHandler: EventHandler) {
        val parameters = mapOf(
                "event_handler" to (true)
        )

        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun setInitialEnterTriggerEnabled(enabled: Boolean) {
        val parameters = mapOf(
                "enabled" to enabled
        )

        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }
}