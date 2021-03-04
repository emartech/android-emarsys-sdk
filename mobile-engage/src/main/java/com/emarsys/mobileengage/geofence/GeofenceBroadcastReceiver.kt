package com.emarsys.mobileengage.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.core.Mockable
import com.emarsys.core.di.getDependency
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
import com.emarsys.mobileengage.geofence.model.TriggerType
import com.emarsys.mobileengage.geofence.model.TriggeringGeofence
import com.google.android.gms.location.GeofencingEvent

@Mockable
class GeofenceBroadcastReceiver(val coreSdkHandler: CoreSdkHandler) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        coreSdkHandler.post {
            val geofenceInternal = getDependency<GeofenceInternal>("defaultInstance")
            if (geofencingEvent.triggeringGeofences != null) {
                geofenceInternal.onGeofenceTriggered(geofencingEvent.triggeringGeofences.map {
                    TriggeringGeofence(it.requestId, convertTransitionToTriggerType(geofencingEvent.geofenceTransition))
                })
                geofencingEvent.triggeringGeofences.forEach {
                    val status = mapOf(
                            "triggerType" to convertTransitionToTriggerType(geofencingEvent.geofenceTransition),
                            "geofenceId" to it.requestId
                    )
                    Logger.debug(StatusLog(GeofenceBroadcastReceiver::class.java, SystemUtils.getCallerMethodName(), mapOf(), status
                    ))
                }
            }
        }
    }

    private fun convertTransitionToTriggerType(transition: Int): TriggerType {
        return when (transition) {
            1 -> TriggerType.ENTER
            2 -> TriggerType.EXIT
            4 -> TriggerType.DWELLING
            else -> TriggerType.ENTER
        }
    }


}