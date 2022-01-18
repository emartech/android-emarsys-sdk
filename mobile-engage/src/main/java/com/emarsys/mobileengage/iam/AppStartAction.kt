package com.emarsys.mobileengage.iam

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.activity.ActivityLifecyclePriorities
import com.emarsys.core.api.proxyApi
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.log.Logger.Companion.info
import com.emarsys.core.util.log.entry.AppEventLog
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.event.EventServiceInternal

class AppStartAction(private val eventServiceInternal: EventServiceInternal, private val contactTokenStorage: Storage<String?>,
                     override val priority: Int = ActivityLifecyclePriorities.APP_START_ACTION_PRIORITY,
                     override val repeatable: Boolean = false,
                     override val triggeringLifecycle: ActivityLifecycleAction.ActivityLifecycle = ActivityLifecycleAction.ActivityLifecycle.RESUME
) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        val coreSdkHandler = mobileEngage().concurrentHandlerHolder
        coreSdkHandler.coreHandler.post {
            if (contactTokenStorage.get() != null) {
                eventServiceInternal.proxyApi(coreSdkHandler)
                    .trackInternalCustomEventAsync("app:start", null, null)
            }
            info(AppEventLog("app:start", null))
        }

    }
}