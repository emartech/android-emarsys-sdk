package com.emarsys.core.activity

import android.app.Activity

interface ActivityLifecycleAction {
    val priority: Int
    val repeatable: Boolean
    val triggeringLifecycle: ActivityLifecycle
    fun execute(activity: Activity?)

    enum class ActivityLifecycle(val priority: Int) {
        CREATE(ActivityLifecyclePriorities.CREATE_PRIORITY),
        RESUME(ActivityLifecyclePriorities.RESUME_PRIORITY);
    }
}

fun ActivityLifecycleAction.getOrderingPriority(): Int {
    return priority + triggeringLifecycle.priority
}