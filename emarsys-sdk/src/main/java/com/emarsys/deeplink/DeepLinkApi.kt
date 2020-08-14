package com.emarsys.deeplink

import android.app.Activity
import android.content.Intent
import com.emarsys.core.api.result.CompletionListener

interface DeepLinkApi {

    fun trackDeepLinkOpen(activity: Activity?, intent: Intent?, completionListener: CompletionListener?)
}