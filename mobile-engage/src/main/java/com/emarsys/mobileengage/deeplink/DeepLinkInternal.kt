package com.emarsys.mobileengage.deeplink

import android.app.Activity
import android.content.Intent
import com.emarsys.core.api.result.CompletionListener

interface DeepLinkInternal {
    fun trackDeepLinkOpen(
        activity: Activity,
        intent: Intent,
        completionListener: CompletionListener?
    )
}