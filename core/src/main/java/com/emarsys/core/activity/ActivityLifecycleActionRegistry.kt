package com.emarsys.core.activity

import android.app.Activity
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider

@Mockable
class ActivityLifecycleActionRegistry(
    val concurrentHandlerHolder: ConcurrentHandlerHolder,
    val currentActivityProvider: CurrentActivityProvider,
    val lifecycleActions: MutableList<ActivityLifecycleAction> = mutableListOf()
) {
    val triggerOnActivityActions: MutableList<ActivityLifecycleAction> = mutableListOf()

    fun execute(activity: Activity?, lifecycles: List<ActivityLifecycleAction.ActivityLifecycle>) {
        concurrentHandlerHolder.post {
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
        concurrentHandlerHolder.post {
            triggerOnActivityActions.add(activityLifecycleAction)
            executePendingOnActivityActions()
        }
    }

    private fun executePendingOnActivityActions() {
        currentActivityProvider.get()?.let { activity ->
            if (triggerOnActivityActions.isNotEmpty()) {
                triggerOnActivityActions.toList().forEach { action ->
                    action.execute(activity)
                }
                triggerOnActivityActions.clear()
            }
        }
    }
}