package com.emarsys.mobileengage.fake

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

class FakeActivityLifecycleCallbacks(
        private val onCreated: (() -> Unit)? = null,
        private val onStarted: (() -> Unit)? = null,
        private val onResume: (() -> Unit)? = null,
        private val onPaused: (() -> Unit)? = null,
        private val onStopped: (() -> Unit)? = null,
        private val onSaveInstanceState: (() -> Unit)? = null,
        private val onDestroyed: (() -> Unit)? = null
) : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        onCreated?.invoke()
    }

    override fun onActivityStarted(activity: Activity) {
        onStarted?.invoke()
    }

    override fun onActivityResumed(activity: Activity) {
        onResume?.invoke()
    }

    override fun onActivityPaused(activity: Activity) {
        onPaused?.invoke()
    }

    override fun onActivityStopped(activity: Activity) {
        onStopped?.invoke()
    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        onSaveInstanceState?.invoke()
    }
    override fun onActivityDestroyed(activity: Activity) {
        onDestroyed?.invoke()
    }
}