package com.emarsys.deeplink

import android.app.Activity
import android.content.Intent
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.di.mobileEngage

class DeepLink(private val loggingInstance: Boolean = false) : DeepLinkApi {
    override fun trackDeepLinkOpen(activity: Activity?, intent: Intent?, completionListener: CompletionListener?) {
        (if (loggingInstance) mobileEngage().loggingDeepLinkInternal else mobileEngage().deepLinkInternal)
                .trackDeepLinkOpen(activity, intent, completionListener)
    }

}