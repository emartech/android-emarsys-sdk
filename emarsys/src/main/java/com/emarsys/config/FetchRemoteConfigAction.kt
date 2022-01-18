package com.emarsys.config

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.activity.ActivityLifecyclePriorities
import com.emarsys.core.api.proxyApi
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.di.mobileEngage

class FetchRemoteConfigAction(private val configInternal: ConfigInternal,
                              override val priority: Int = ActivityLifecyclePriorities.FETCH_REMOTE_CONFIG_ACTION_PRIORITY,
                              override val repeatable: Boolean = false,
                              override val triggeringLifecycle: ActivityLifecycleAction.ActivityLifecycle = ActivityLifecycleAction.ActivityLifecycle.RESUME,
                              private val completionListener: CompletionListener
) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        configInternal.proxyApi(mobileEngage().concurrentHandlerHolder)
                .refreshRemoteConfig(completionListener)
    }
}
