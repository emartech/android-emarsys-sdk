package com.emarsys.mobileengage.notification.command

import android.content.Context
import android.content.Intent
import com.emarsys.core.Mockable
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog

@Mockable
class OpenExternalUrlCommand(val intent: Intent, val context: Context) : Runnable {
    override fun run() {
        try {
            context.startActivity(intent)
        } catch (exception: Exception) {
            Logger.debug(StatusLog(OpenExternalUrlCommand::class.java, SystemUtils.getCallerMethodName(), mapOf(
                    "intent" to intent,
                    "exception" to exception
            )))
        }
    }
}