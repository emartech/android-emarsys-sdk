package com.emarsys.mobileengage.geofence

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.activity.ActivityLifecyclePriorities
import com.emarsys.core.api.proxyApi
import com.emarsys.mobileengage.di.mobileEngage

class FetchGeofencesAction(private val geofenceInternal: GeofenceInternal,
                           override val priority: Int = ActivityLifecyclePriorities.FETCH_GEOFENCE_ACTION_PRIORITY,
                           override val repeatable: Boolean = false,
                           override val triggeringLifecycle: ActivityLifecycleAction.ActivityLifecycle = ActivityLifecycleAction.ActivityLifecycle.CREATE
) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        geofenceInternal.proxyApi(mobileEngage().concurrentHandlerHolder).fetchGeofences(null)
    }
}