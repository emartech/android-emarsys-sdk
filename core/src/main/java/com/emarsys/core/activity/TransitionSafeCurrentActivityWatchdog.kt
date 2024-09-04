package com.emarsys.core.activity

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.emarsys.core.Mockable
import com.emarsys.core.handler.SdkHandler
import com.emarsys.core.observer.Observer
import java.util.concurrent.CountDownLatch

@Mockable
class TransitionSafeCurrentActivityWatchdog(private val handler: SdkHandler) :
    ActivityLifecycleCallbacks, Observer<Activity> {

    private val activityCallbacks = mutableListOf<(Activity) -> Unit>()

    private var currentActivity: Activity? = null

    private val callback: Runnable = Runnable {
        if (currentActivity != null) {
            notify(currentActivity!!)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = null
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = null
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        handler.postDelayed(callback, 500)
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity == currentActivity) {
            currentActivity = null
            handler.remove(callback)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity == currentActivity) {
            currentActivity = null
            handler.remove(callback)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (activity == currentActivity) {
            currentActivity = null
            handler.remove(callback)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == currentActivity) {
            currentActivity = null
            handler.remove(callback)
        }
    }

    override fun register(callback: (Activity) -> Unit) {
        activityCallbacks.add(callback)
        if (currentActivity != null) {
            callback(currentActivity!!)
        }
    }

    override fun unregister(callback: (Activity) -> Unit) {
        activityCallbacks.remove(callback)
    }

    override fun notify(value: Activity) {
        activityCallbacks.forEach { it(value) }
    }

    fun activity(): Activity {
        lateinit var result: Activity
        val latch = CountDownLatch(1)
        val callback: (Activity) -> Unit = {
            result = it
            latch.countDown()
        }
        register(callback)
        latch.await()
        unregister(callback)
        return result
    }

}
