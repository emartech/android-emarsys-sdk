package com.emarsys.mobileengage.notification

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.CountDownLatch

class LaunchActivityCommandLifecycleCallbacks(private val latch: CountDownLatch) : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {
        val launchIntent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)

        if (launchIntent?.resolveActivity(activity.packageManager)?.shortClassName?.endsWith(activity.localClassName) == true) {
            latch.countDown()
        }
    }
}