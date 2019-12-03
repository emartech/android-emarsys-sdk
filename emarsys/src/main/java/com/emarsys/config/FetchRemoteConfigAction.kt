package com.emarsys.config

import android.app.Activity
import com.emarsys.config.model.RemoteConfig

import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.api.result.ResultListener

class FetchRemoteConfigAction(private val configInternal: ConfigInternal) : ActivityLifecycleAction {

    override fun execute(activity: Activity) {
        configInternal.fetchRemoteConfig(ResultListener {
            it.result?.let { remoteConfig ->
                configInternal.applyRemoteConfig(remoteConfig)
            }
            it.errorCause?.let {
                configInternal.applyRemoteConfig(RemoteConfig())
            }
        })
    }
}
