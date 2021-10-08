package com.emarsys.core.activity

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.emarsys.core.Mockable
import com.emarsys.core.provider.Property

@Mockable
class CurrentActivityWatchdog(private val currentActivityProvider: Property<Activity?>) : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        currentActivityProvider.set(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivityProvider.get() === activity) {
            currentActivityProvider.set(null)
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}