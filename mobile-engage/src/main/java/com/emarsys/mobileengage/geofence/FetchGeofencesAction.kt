package com.emarsys.mobileengage.geofence

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.api.proxyApi
import com.emarsys.mobileengage.di.mobileEngage

class FetchGeofencesAction(private val geofenceInternal: GeofenceInternal) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        geofenceInternal.proxyApi(mobileEngage().coreSdkHandler).fetchGeofences(null)
    }
}