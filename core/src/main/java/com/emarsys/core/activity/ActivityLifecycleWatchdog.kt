package com.emarsys.core.activity

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.emarsys.core.Mockable
import com.emarsys.core.activity.ActivityLifecycleAction.ActivityLifecycle
import com.emarsys.getCurrentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Mockable
class ActivityLifecycleWatchdog(
    val activityLifecycleActionRegistry: ActivityLifecycleActionRegistry
) : ActivityLifecycleCallbacks {

    init {
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            onActivityCreated(getCurrentActivity(), null)
            onActivityResumed(getCurrentActivity())
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityLifecycleActionRegistry.execute(activity, listOf(ActivityLifecycle.CREATE))
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        activityLifecycleActionRegistry.execute(activity, listOf(ActivityLifecycle.RESUME))
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}