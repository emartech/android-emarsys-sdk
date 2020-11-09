package com.emarsys.mobileengage.deeplink

import android.app.Activity
import android.content.Intent
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed

class LoggingDeepLinkInternal(private val klass: Class<*>) : DeepLinkInternal {
    override fun trackDeepLinkOpen(activity: Activity, intent: Intent, completionListener: CompletionListener?) {
        val parameters = mapOf(
                "activity" to activity.toString(),
                "intent" to intent.toString(),
                "completion_listener" to (completionListener != null)

        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }
}