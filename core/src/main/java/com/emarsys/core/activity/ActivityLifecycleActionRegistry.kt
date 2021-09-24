package com.emarsys.core.activity

import android.app.Activity
import com.emarsys.core.Mockable
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.activity.CurrentActivityProvider

@Mockable
class ActivityLifecycleActionRegistry(
    val coreSdkHandler: CoreSdkHandler,
    val currentActivityProvider: CurrentActivityProvider,
    val lifecycleActions: MutableList<ActivityLifecycleAction> = mutableListOf()
) {
    val triggerOnActivityActions: MutableList<ActivityLifecycleAction> = mutableListOf()

    fun execute(activity: Activity?, lifecycles: List<ActivityLifecycleAction.ActivityLifecycle>) {
        coreSdkHandler.post {
            (lifecycleActions + triggerOnActivityActions)
                .filter {
                    lifecycles.contains(it.triggeringLifecycle)
                }
                .sortedBy {
                    it.getOrderingPriority()
                }
                .forEach { action ->
                    action.execute(activity)
                    if (!action.repeatable) {
                        lifecycleActions.remove(action)
                        triggerOnActivityActions.remove(action)
                    }
                }
        }
    }

    fun addTriggerOnActivityAction(activityLifecycleAction: ActivityLifecycleAction) {
        coreSdkHandler.post {
            val currentActivity = currentActivityProvider.get()
            triggerOnActivityActions.add(activityLifecycleAction)
            if (currentActivity != null) {
                triggerOnActivityActions.forEach { action ->
                    action.execute(currentActivity)
                }
                triggerOnActivityActions.clear()
            }
        }
    }
}