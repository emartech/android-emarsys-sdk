package com.emarsys.mobileengage.notification

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.CountDownLatch

class LaunchActivityCommandLifecycleCallbacksFactoryTest : AnnotationSpec() {
    @Test
    fun testCreate() {
        val latch = CountDownLatch(1)
        val result = LaunchActivityCommandLifecycleCallbacksFactory().create(latch)
        result::class.java shouldBe LaunchActivityCommandLifecycleCallbacks::class.java
    }
}