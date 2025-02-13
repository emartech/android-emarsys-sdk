package com.emarsys.core.activity

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.emarsys.core.Mockable
import com.emarsys.core.activity.ActivityLifecycleAction.ActivityLifecycle
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
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
            val activity = getCurrentActivity()
            if (activity != null) {
                onActivityCreated(activity, null)
                onActivityResumed(activity)
            } else {
                Logger.error(
                    StatusLog(
                        this::class.java,
                        "ActivityLifecycleWatchdog#init",
                        mapOf("activity" to "null")
                    )
                )
            }
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