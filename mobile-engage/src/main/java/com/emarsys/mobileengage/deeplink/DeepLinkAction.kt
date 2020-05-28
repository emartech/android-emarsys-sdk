package com.emarsys.mobileengage.deeplink

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.api.proxyApi
import com.emarsys.core.di.getDependency

class DeepLinkAction(private val deepLinkInternal: DeepLinkInternal) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        if (activity != null && activity.intent != null) {
            deepLinkInternal.proxyApi(getDependency("coreSdkHandler")).trackDeepLinkOpen(activity, activity.intent, null)
        }
    }
}