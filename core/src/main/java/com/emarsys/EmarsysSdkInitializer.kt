package com.emarsys

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import androidx.startup.Initializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal var currentActivityFlow: MutableStateFlow<WeakReference<Activity?>?> =
    MutableStateFlow(null)
    private set

suspend fun getCurrentActivity(): Activity? {
    return currentActivityFlow.first { activityReference -> activityReference?.get() != null }
        ?.get()
}

class EmarsysSdkInitializer : Initializer<Unit> {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var setActivityJob: Job? = null

    override fun create(context: Context) {
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(object :
            ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivityFlow.value = null
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivityFlow.value = null
            }

            override fun onActivityResumed(activity: Activity) {
                setActivityJob = scope.launch {
                    delay(500)
                    currentActivityFlow.value = WeakReference(activity)
                }
            }

            override fun onActivityPaused(activity: Activity) {
                if (activity == currentActivityFlow.value) {
                    currentActivityFlow.value = null
                    setActivityJob?.cancel()
                }
            }

            override fun onActivityStopped(activity: Activity) {
                if (activity == currentActivityFlow.value) {
                    currentActivityFlow.value = null
                    setActivityJob?.cancel()
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                if (activity == currentActivityFlow.value) {
                    currentActivityFlow.value = null
                    setActivityJob?.cancel()
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (activity == currentActivityFlow.value) {
                    currentActivityFlow.value = null
                    setActivityJob?.cancel()
                }
            }
        })
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}

