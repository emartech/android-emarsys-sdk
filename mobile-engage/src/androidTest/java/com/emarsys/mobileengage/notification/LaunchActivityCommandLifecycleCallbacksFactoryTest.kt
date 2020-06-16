package com.emarsys.mobileengage.notification

import io.kotlintest.shouldBe
import org.junit.Test
import java.util.concurrent.CountDownLatch

class LaunchActivityCommandLifecycleCallbacksFactoryTest {
    @Test
    fun testCreate() {
        val latch = CountDownLatch(1)
        val result = LaunchActivityCommandLifecycleCallbacksFactory().create(latch)
        result::class.java shouldBe LaunchActivityCommandLifecycleCallbacks::class.java
    }
}