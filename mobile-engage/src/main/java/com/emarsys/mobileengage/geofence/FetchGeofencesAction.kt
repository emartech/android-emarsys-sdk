package com.emarsys.mobileengage.geofence

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction

class FetchGeofencesAction(private val geofenceInternal: GeofenceInternal) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        geofenceInternal.fetchGeofences(null)
    }
}