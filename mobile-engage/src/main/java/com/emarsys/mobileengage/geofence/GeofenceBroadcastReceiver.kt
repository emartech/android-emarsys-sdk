package com.emarsys.mobileengage.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.core.Mockable
import com.emarsys.core.di.Container.getDependency
import com.emarsys.mobileengage.geofence.model.TriggerType
import com.emarsys.mobileengage.geofence.model.TriggeringGeofence
import com.google.android.gms.location.GeofencingEvent

@Mockable
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        val geofenceInternal = getDependency<GeofenceInternal>("defaultInstance")
        if (geofencingEvent.triggeringGeofences != null) {
            geofenceInternal.onGeofenceTriggered(geofencingEvent.triggeringGeofences.map {
                TriggeringGeofence(it.requestId, convertTransitionToTriggerType(geofencingEvent.geofenceTransition))
            })
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