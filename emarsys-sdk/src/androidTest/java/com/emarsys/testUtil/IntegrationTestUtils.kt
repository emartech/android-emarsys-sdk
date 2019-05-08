package com.emarsys.testUtil

import com.emarsys.Emarsys
import io.kotlintest.shouldBe
import java.util.concurrent.CountDownLatch

object IntegrationTestUtils {

    @JvmStatic
    fun doAppLogin(contactFieldValue: String = "test@test.com") {
        val latch = CountDownLatch(1)
        var errorCause: Throwable? = null
        Emarsys.setContact(contactFieldValue) {
            errorCause = it
            latch.countDown()
        }
        latch.await()
        errorCause shouldBe null
    }

    @JvmStatic
    fun doSetPushToken(pushToken: String = "integration_test_push_token") {
        val latch = CountDownLatch(1)
        var errorCause: Throwable? = null
        Emarsys.Push.setPushToken(pushToken) {
            errorCause = it
            latch.countDown()
        }
        latch.await()
        errorCause shouldBe null
    }

    fun doLogin(contactFieldValue: String = "test@test.com", pushToken: String = "integration_test_push_token") {
        val latchForPushToken = CountDownLatch(2)
        var errorCause: Throwable? = null
        Emarsys.Push.setPushToken(pushToken) { throwable ->
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
}