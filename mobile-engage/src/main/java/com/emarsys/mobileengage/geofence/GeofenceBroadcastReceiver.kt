package com.emarsys.mobileengage.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
import com.emarsys.mobileengage.api.geofence.TriggerType
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.geofence.model.TriggeringEmarsysGeofence
import com.google.android.gms.location.GeofencingEvent


class GeofenceBroadcastReceiver :
    BroadcastReceiver() {
    private val concurrentHandlerHolder by lazy {
        mobileEngage().concurrentHandlerHolder
    }
    private val timestampProvider by lazy {
        mobileEngage().timestampProvider
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.triggeringGeofences != null) {
            val triggerTimestamp = timestampProvider.provideTimestamp()
            concurrentHandlerHolder.coreHandler.post {
                val geofenceInternal = mobileEngage().geofenceInternal

                val triggeringEmarsysGeofences =
                    geofencingEvent.convertToTriggeringEmarsysGeofences(triggerTimestamp)
                geofenceInternal.onGeofenceTriggered(triggeringEmarsysGeofences)

                logTriggeringGeofences(triggeringEmarsysGeofences)
            }
        }
    }

    private fun GeofencingEvent.convertToTriggeringEmarsysGeofences(triggerTimestamp: Long): List<TriggeringEmarsysGeofence> {
        return this.triggeringGeofences?.map {
            TriggeringEmarsysGeofence(
                it.requestId,
                convertTransitionToTriggerType(this.geofenceTransition),
                triggerTimestamp
            )
        } ?: emptyList()
    }

    private fun convertTransitionToTriggerType(transition: Int): TriggerType {
        return when (transition) {
            1 -> TriggerType.ENTER
            2 -> TriggerType.EXIT
            4 -> TriggerType.DWELLING
            else -> TriggerType.ENTER
        }
    }

    private fun logTriggeringGeofences(triggeringEmarsysGeofences: List<TriggeringEmarsysGeofence>) {
        triggeringEmarsysGeofences.forEach {
            val status = mapOf(
                "triggerType" to it.triggerType,
                "geofenceId" to it.geofenceId
            )
            Logger.debug(
                StatusLog(
                    GeofenceBroadcastReceiver::class.java,
                    SystemUtils.getCallerMethodName(),
                    mapOf(),
                    status
                )
            )
        }
    }


}