package com.emarsys.testUtil

import android.app.Application

import com.emarsys.di.emarsys
import com.emarsys.di.tearDownEmarsysComponent

object E2ETestUtils {

    fun tearDownEmarsys(application: Application? = null) {
        FeatureTestUtils.resetFeatures()

        emarsys().coreSdkHandler.post {
            if (application != null) {
                application.unregisterActivityLifecycleCallbacks(emarsys().activityLifecycleWatchdog)
                application.unregisterActivityLifecycleCallbacks(emarsys().currentActivityWatchdog)
            }

            emarsys().contactTokenStorage.remove()
            emarsys().clientStateStorage.remove()
            emarsys().pushTokenStorage.remove()
            emarsys().hardwareIdStorage.remove()
            emarsys().refreshTokenStorage.remove()
            emarsys().contactFieldValueStorage.remove()
            emarsys().clientServiceStorage.remove()
            emarsys().eventServiceStorage.remove()
            emarsys().deepLinkServiceStorage.remove()
            emarsys().messageInboxServiceStorage.remove()
            emarsys().deviceEventStateStorage.remove()
            emarsys().deviceInfoPayloadStorage.remove()
            emarsys().logLevelStorage.remove()
            emarsys().predictServiceStorage.remove()
        }
        emarsys().coreSdkHandler.looper.quitSafely()

        tearDownEmarsysComponent()
    }

    fun retry(times: Int = 3, retryInterval: Long = 1000, action: () -> Unit) {
        try {
            action.invoke()
        } catch (e: Throwable) {
            if (times > 0) {
                Thread.sleep(retryInterval)
                retry(times - 1, retryInterval, action)
            } else {
                throw e
            }
        }
    }
}