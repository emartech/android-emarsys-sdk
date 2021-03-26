package com.emarsys.testUtil

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.arch.core.internal.FastSafeIterableMap
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.emarsys.Emarsys
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.predict.storage.PredictStorageKey
import io.kotlintest.shouldBe
import java.util.concurrent.CountDownLatch

object IntegrationTestUtils {

    fun doLogin(contactFieldValue: String = "test@test.com", pushToken: String = "integration_test_push_token") {
        val latchForPushToken = CountDownLatch(2)
        var errorCause: Throwable? = null
        Emarsys.push.setPushToken(pushToken) { throwable ->
            errorCause = throwable
            latchForPushToken.countDown()
            Emarsys.setContact(contactFieldValue) {
                errorCause = it
                latchForPushToken.countDown()
            }
        }
        latchForPushToken.await()
        errorCause shouldBe null
    }

    fun tearDownEmarsys(application: Application? = null) {
        var latch = CountDownLatch(1)
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
            latch.countDown()
        }
        getDependency<CoreSdkHandler>().looper.quitSafely()
        latch.await()

        latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post {
            val observerMap = ReflectionTestUtils.getInstanceField<FastSafeIterableMap<Any, Any>>(
                    ProcessLifecycleOwner.get().lifecycle,
                    "mObserverMap")
            if (observerMap != null) {
                ReflectionTestUtils.getInstanceField<HashMap<Any, Any>>(
                        observerMap,
                        "mHashMap")?.entries?.forEach {
                    ProcessLifecycleOwner.get().lifecycle.removeObserver(it.key as LifecycleObserver)
                }
            }
            latch.countDown()
        }
        latch.await()

        DependencyInjection.tearDown()

        FeatureTestUtils.resetFeatures()
    }

    fun <T> runOnUiThread(lambda: () -> T): T {
        var result: T? = null
        val latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post {
            result = lambda.invoke()
            latch.countDown()
        }
        latch.await()
        return result!!
    }

    fun retry(times: Int = 20, timeout: Long = 1000, action: () -> Unit) {
        try {
            action.invoke()
        } catch (e: Throwable) {
            if (times > 0) {
                retry(times - 1, timeout) {
                    Thread.sleep(timeout)
                    action.invoke()
                }
            } else {
                throw e
            }
        }
    }
}