package com.emarsys.mobileengage.device

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.activity.ActivityLifecycleAction.ActivityLifecycle.RESUME
import com.emarsys.core.activity.ActivityLifecyclePriorities
import com.emarsys.core.api.proxyApi
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.di.mobileEngage

class DeviceInfoStartAction(private val clientInternal: ClientServiceInternal,
                            private val deviceInfoPayloadStorage: Storage<String?>,
                            private val deviceInfo: DeviceInfo,
                            override val triggeringLifecycle: ActivityLifecycleAction.ActivityLifecycle = RESUME,
                            override val priority: Int = ActivityLifecyclePriorities.DEVICE_INFO_START_ACTION_PRIORITY,
                            override val repeatable: Boolean = true
) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        val coreSdkHandler = mobileEngage().concurrentHandlerHolder
        coreSdkHandler.coreHandler.post {
            if (deviceInfoPayloadStorage.get() == null || deviceInfoPayloadStorage.get() != deviceInfo.deviceInfoPayload) {
                clientInternal.proxyApi(coreSdkHandler).trackDeviceInfo(null)
            }
        }
    }
}