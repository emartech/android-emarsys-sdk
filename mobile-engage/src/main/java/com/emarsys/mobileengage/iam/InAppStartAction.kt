package com.emarsys.mobileengage.iam

import android.app.Activity
import android.os.Handler
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.api.proxyApi
import com.emarsys.core.di.getDependency
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.log.Logger.Companion.info
import com.emarsys.core.util.log.entry.AppEventLog
import com.emarsys.mobileengage.event.EventServiceInternal

class InAppStartAction(private val eventServiceInternal: EventServiceInternal, private val contactTokenStorage: Storage<String?>) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        val coreSdkHandler = getDependency<Handler>("coreSdkHandler")
        coreSdkHandler.post {
            if (contactTokenStorage.get() != null) {
                eventServiceInternal.proxyApi(coreSdkHandler).trackInternalCustomEvent("app:start", null, null)
            }
            info(AppEventLog("app:start", null))
        }

    }
}