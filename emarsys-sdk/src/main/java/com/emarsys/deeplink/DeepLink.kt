package com.emarsys.deeplink

import android.app.Activity
import android.content.Intent
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.getDependency
import com.emarsys.mobileengage.deeplink.DeepLinkInternal

class DeepLink(private val loggingInstance: Boolean = false) : DeepLinkApi {
    override fun trackDeepLinkOpen(activity: Activity?, intent: Intent?, completionListener: CompletionListener?) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<DeepLinkInternal>("defaultInstance"))
                .trackDeepLinkOpen(activity, intent, completionListener)
    }

}