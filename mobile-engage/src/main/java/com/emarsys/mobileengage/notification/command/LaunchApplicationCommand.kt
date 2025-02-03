package com.emarsys.mobileengage.notification.command

import android.app.Application
import android.app.PendingIntent.CanceledException
import android.content.Context
import android.content.Intent
import com.emarsys.core.util.log.Logger.Companion.error
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.mobileengage.notification.LaunchActivityCommandLifecycleCallbacksFactory
import com.emarsys.mobileengage.service.IntentUtils.createLaunchPendingIntent
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LaunchApplicationCommand(
    private val intent: Intent,
    private val context: Context,
    private val launchActivityCommandLifecycleCallbacksFactory: LaunchActivityCommandLifecycleCallbacksFactory
) : Runnable {

    override fun run() {
        val latch = CountDownLatch(1)
        val callback = launchActivityCommandLifecycleCallbacksFactory.create(latch)
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(callback)
        val launchPendingIntent = createLaunchPendingIntent(intent, context)

        launchPendingIntent?.let {
            try {
                launchPendingIntent.send()
                latch.await(5, TimeUnit.SECONDS)
            } catch (e: CanceledException) {
                error(CrashLog(e, null))
            } catch (e: InterruptedException) {
                error(CrashLog(e, null))
            }
        }

        (context.applicationContext as Application).unregisterActivityLifecycleCallbacks(callback)
    }
}
