package com.emarsys.mobileengage.deeplink

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.api.proxyApi
import com.emarsys.mobileengage.di.mobileEngage

class DeepLinkAction(private val deepLinkInternal: DeepLinkInternal) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        if (activity != null && activity.intent != null) {
            deepLinkInternal.proxyApi(mobileEngage().coreSdkHandler).trackDeepLinkOpen(activity, activity.intent, null)
        }
    }
}