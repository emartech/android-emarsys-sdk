package com.emarsys.mobileengage.deeplink

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.activity.ActivityLifecyclePriorities
import com.emarsys.core.api.proxyApi
import com.emarsys.mobileengage.di.mobileEngage

class DeepLinkAction(private val deepLinkInternal: DeepLinkInternal,
                     override val priority: Int = ActivityLifecyclePriorities.DEEP_LINK_ACTION_PRIORITY,
                     override val repeatable: Boolean = true,
                     override val triggeringLifecycle: ActivityLifecycleAction.ActivityLifecycle = ActivityLifecycleAction.ActivityLifecycle.CREATE
) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        if (activity != null && activity.intent != null) {
            deepLinkInternal.proxyApi(mobileEngage().concurrentHandlerHolder)
                .trackDeepLinkOpen(activity, activity.intent, null)
        }
    }
}