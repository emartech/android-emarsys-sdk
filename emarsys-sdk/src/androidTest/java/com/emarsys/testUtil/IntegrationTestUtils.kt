package com.emarsys.testUtil


import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.arch.core.internal.FastSafeIterableMap
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.emarsys.Emarsys
import com.emarsys.di.emarsys
import com.emarsys.di.tearDownEmarsysComponent
import io.kotlintest.shouldBe
import java.util.concurrent.CountDownLatch

object IntegrationTestUtils {

    fun doLogin(
        contactFieldId: Int = 2575,
        contactFieldValue: String = "test@test.com",
        pushToken: String = "integration_test_push_token"
    ) {
        val latchForPushToken = CountDownLatch(2)
        var errorCause: Throwable? = null
        Emarsys.push.setPushToken(pushToken) { throwable ->
            errorCause = throwable
            latchForPushToken.countDown()
            Emarsys.setContact(contactFieldId, contactFieldValue) {
                errorCause = it
                latchForPushToken.countDown()
            }
        }
        latchForPushToken.await()
        errorCause shouldBe null
    }

    @Synchronized
    fun tearDownEmarsys(application: Application? = null) {
        var latch = CountDownLatch(1)
        emarsys().concurrentHandlerHolder.coreHandler.post {
            if (application != null) {
                application.unregisterActivityLifecycleCallbacks(emarsys().activityLifecycleWatchdog)
                application.unregisterActivityLifecycleCallbacks(emarsys().currentActivityWatchdog)
            }

            emarsys().clientStateStorage.remove()
            emarsys().contactFieldValueStorage.remove()
            emarsys().contactTokenStorage.remove()
            emarsys().pushTokenStorage.remove()
            emarsys().refreshTokenStorage.remove()
            emarsys().deviceInfoPayloadStorage.remove()

            emarsys().clientServiceStorage.remove()
            emarsys().eventServiceStorage.remove()
            emarsys().deepLinkServiceStorage.remove()
            emarsys().messageInboxServiceStorage.remove()
            emarsys().predictServiceStorage.remove()
            latch.countDown()
        }
        emarsys().concurrentHandlerHolder.looper.quitSafely()
        latch.await()

        latch = CountDownLatch(1)
        emarsys().uiHandler.post {
            val observerMap = ReflectionTestUtils.getInstanceField<FastSafeIterableMap<Any, Any>>(
                ProcessLifecycleOwner.get().lifecycle,
                "mObserverMap"
            )
            if (observerMap != null) {
                    ReflectionTestUtils.getInstanceField<HashMap<Any, Any>>(
                        observerMap,
                        "mHashMap"
                    )?.entries?.toMutableList()?.forEach {
                        ProcessLifecycleOwner.get().lifecycle.removeObserver(it.key as LifecycleObserver)
                    }
            }
            latch.countDown()
        }
        latch.await()

        tearDownEmarsysComponent()

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