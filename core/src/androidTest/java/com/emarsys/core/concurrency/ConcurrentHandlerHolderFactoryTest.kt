package com.emarsys.core.concurrency

import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


class ConcurrentHandlerHolderFactoryTest : AnnotationSpec() {
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
    }

    @After
    fun tearDown() {
        concurrentHandlerHolder.coreLooper.quit()
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
        val actualName = concurrentHandlerHolder.coreLooper.thread.name
        actualName.startsWith(expectedNamePrefix) shouldBe true
    }
}