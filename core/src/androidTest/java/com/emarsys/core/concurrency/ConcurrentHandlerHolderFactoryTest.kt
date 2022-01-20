package com.emarsys.core.concurrency

import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ConcurrentHandlerHolderFactoryTest {
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
    }

    @After
    fun tearDown() {
        concurrentHandlerHolder.looper.quit()
    }

    @Test
    fun testProvideHandler_shouldNotReturnNull() {
        concurrentHandlerHolder shouldNotBe null
    }

    @Test
    fun testProvideHandler_shouldReturnConcurrentHandlerHolder() {
        concurrentHandlerHolder.javaClass shouldBe ConcurrentHandlerHolder::class.java
    }

    @Test
    fun testProvideHandler_shouldReturnConcurrentHandlerHolderWithCorrectName() {
        val expectedNamePrefix = "CoreSDKHandlerThread"
        val actualName = concurrentHandlerHolder.looper.thread.name
        actualName.startsWith(expectedNamePrefix) shouldBe true
    }
}