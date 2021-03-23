package com.emarsys.testUtil

import android.app.Application
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.predict.storage.PredictStorageKey

object E2ETestUtils {

    fun tearDownEmarsys(application: Application? = null) {
        FeatureTestUtils.resetFeatures()

        getDependency<CoreSdkHandler>().post {
            if (application != null) {
                application.unregisterActivityLifecycleCallbacks(getDependency<ActivityLifecycleWatchdog>())
                application.unregisterActivityLifecycleCallbacks(getDependency<CurrentActivityWatchdog>())
            }

            getDependency<StringStorage>(MobileEngageStorageKey.DEVICE_INFO_HASH.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.REFRESH_TOKEN.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_STATE.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.CONTACT_FIELD_VALUE.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.CONTACT_TOKEN.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.PUSH_TOKEN.key).remove()

            getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.EVENT_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.ME_V2_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.INBOX_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key).remove()
            getDependency<StringStorage>(PredictStorageKey.PREDICT_SERVICE_URL.key).remove()
        }
        getDependency<CoreSdkHandler>().looper.quitSafely()

        DependencyInjection.tearDown()
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