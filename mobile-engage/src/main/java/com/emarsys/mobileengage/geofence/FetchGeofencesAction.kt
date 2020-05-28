package com.emarsys.mobileengage.geofence

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.api.proxyApi
import com.emarsys.core.di.getDependency

class FetchGeofencesAction(private val geofenceInternal: GeofenceInternal) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        geofenceInternal.proxyApi(getDependency("coreSdkHandler")).fetchGeofences(null)
    }
}