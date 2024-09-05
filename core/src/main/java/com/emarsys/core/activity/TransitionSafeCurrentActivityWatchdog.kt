package com.emarsys.core.activity

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.emarsys.core.Mockable
import com.emarsys.core.handler.SdkHandler
import com.emarsys.core.observer.Observer
import com.emarsys.core.provider.Property
import com.emarsys.getCurrentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

@Mockable
class TransitionSafeCurrentActivityWatchdog(
    private val handler: SdkHandler,
    private val currentActivityProvider: Property<Activity?>
) :
    ActivityLifecycleCallbacks, Observer<Activity> {

    private val activityCallbacks = mutableListOf<(Activity) -> Unit>()

    private var mCurrentActivity: Activity? = null

    private val callback: Runnable = Runnable {
        if (mCurrentActivity != null) {
            notify(mCurrentActivity!!)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        mCurrentActivity = null
    }

    override fun onActivityStarted(activity: Activity) {
        mCurrentActivity = null
    }

    override fun onActivityResumed(activity: Activity) {
        mCurrentActivity = activity
        handler.postDelayed(callback, 500)
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity == mCurrentActivity) {
            mCurrentActivity = null
            handler.remove(callback)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity == mCurrentActivity) {
            mCurrentActivity = null
            handler.remove(callback)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (activity == mCurrentActivity) {
            mCurrentActivity = null
            handler.remove(callback)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == mCurrentActivity) {
            mCurrentActivity = null
            handler.remove(callback)
        }
    }

    override fun register(callback: (Activity) -> Unit) {
        activityCallbacks.add(callback)
        CoroutineScope(Dispatchers.Default).launch {
            val currentActivity = getCurrentActivity()
            currentActivityProvider.set(currentActivity)
            callback(currentActivity)
        }
    }

    override fun unregister(callback: (Activity) -> Unit) {
        activityCallbacks.remove(callback)
    }

    override fun notify(value: Activity) {
        currentActivityProvider.set(value)
        activityCallbacks.forEach { it(value) }
    }

    fun activity(): Activity {
        if (mCurrentActivity != null) {
            return mCurrentActivity!!
        }
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
