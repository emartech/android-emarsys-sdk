package com.emarsys.config

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.api.proxyApi
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection

class FetchRemoteConfigAction(private val configInternal: ConfigInternal) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        configInternal.proxyApi(DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler()).refreshRemoteConfig(null)
    }
}
