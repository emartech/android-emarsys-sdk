package com.emarsys.testUtil

import com.emarsys.Emarsys
import io.kotlintest.shouldBe
import java.util.concurrent.CountDownLatch

object IntegrationTestUtils {

    @JvmStatic
    fun doAppLogin(contactFieldValue: String = "test@test.com") {
        val latch = CountDownLatch(1)
        var errorCause: Throwable? = null
        Emarsys.setCustomer(contactFieldValue) {
            errorCause = it
            latch.countDown()
        }
        latch.await()
        errorCause shouldBe null
    }

}