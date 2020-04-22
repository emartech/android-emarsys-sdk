package com.emarsys.config

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction

class FetchRemoteConfigAction(private val configInternal: ConfigInternal) : ActivityLifecycleAction {

    override fun execute(activity: Activity) {
        configInternal.refreshRemoteConfig(null)
    }
}
