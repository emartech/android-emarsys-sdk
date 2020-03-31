package com.emarsys.core.activity

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.emarsys.core.Mockable
import com.emarsys.core.util.Assert
import java.util.*

@Mockable
class ActivityLifecycleWatchdog(val applicationStartActions: Array<ActivityLifecycleAction> = emptyArray(),
                                val activityCreatedActions: Array<ActivityLifecycleAction> = emptyArray(),
                                val initializationActions: Array<ActivityLifecycleAction?> = emptyArray())
    : ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null
    val triggerOnActivityActions: MutableList<ActivityLifecycleAction> = mutableListOf()

    fun addTriggerOnActivityAction(triggerOnActivityAction: ActivityLifecycleAction) {
        triggerOnActivityActions.add(triggerOnActivityAction)
        if (currentActivity != null) {
            triggerOnActivity()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        for (action in activityCreatedActions) {
            action.execute(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        if (currentActivity == null) {
            initializationActions.forEachIndexed { index, action ->
                action?.execute(activity)
                initializationActions[index] = null
            }
            for (action in applicationStartActions) {
                action.execute(activity)
            }
        }
        currentActivity = activity
        triggerOnActivity()
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {
        if (currentActivity === activity) {
            currentActivity = null
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}
    private fun triggerOnActivity() {
        for (i in triggerOnActivityActions.indices.reversed()) {
            val action = triggerOnActivityActions.removeAt(i)
            action.execute(currentActivity)
        }
    }
}