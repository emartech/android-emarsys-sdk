package com.emarsys.mobileengage.device

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.api.proxyApi
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.di.mobileEngage

class DeviceInfoStartAction(private val clientInternal: ClientServiceInternal, private val deviceInfoPayloadStorage: Storage<String?>, private val deviceInfo: DeviceInfo) : ActivityLifecycleAction {

    override fun execute(activity: Activity?) {
        val coreSdkHandler = mobileEngage().coreSdkHandler
        coreSdkHandler.post {
            if (deviceInfoPayloadStorage.get() == null || deviceInfoPayloadStorage.get() != deviceInfo.deviceInfoPayload) {
                clientInternal.proxyApi(coreSdkHandler).trackDeviceInfo(null)
            }
        }
    }
}