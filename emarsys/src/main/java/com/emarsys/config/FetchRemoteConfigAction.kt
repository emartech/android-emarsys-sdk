package com.emarsys.config

import android.app.Activity
import android.util.Log
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.activity.ActivityLifecyclePriorities
import com.emarsys.core.api.proxyApi
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
import com.emarsys.mobileengage.di.mobileEngage

class FetchRemoteConfigAction(private val configInternal: ConfigInternal,
                              override val priority: Int = ActivityLifecyclePriorities.FETCH_REMOTE_CONFIG_ACTION_PRIORITY,
                              override val repeatable: Boolean = false,
                              override val triggeringLifecycle: ActivityLifecycleAction.ActivityLifecycle = ActivityLifecycleAction.ActivityLifecycle.RESUME,
                              private val completionListener: CompletionListener
) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        if (!listOf("", "null", "nil", "0").contains(configInternal.applicationCode?.lowercase())) {
            configInternal.proxyApi(mobileEngage().concurrentHandlerHolder)
                .refreshRemoteConfig(completionListener)
        } else {
            Log.w("EmarsysSdk", "Invalid applicationCode: ${configInternal.applicationCode}")
            val logEntry = StatusLog(FetchRemoteConfigAction::class.java, "execute", mapOf("applicationCode" to configInternal.applicationCode))
            Logger.log(logEntry)
        }
    }
}
