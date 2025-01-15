package com.emarsys.core.api

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.di.FakeCoreDependencyContainer
import com.emarsys.core.di.setupCoreComponent
import com.emarsys.core.di.tearDownCoreComponent
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.log.LogLevel
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CountDownLatch


class LogExceptionProxyTest : AnnotationSpec() {
    private lateinit var mockLogger: Logger
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockLogger = mockk(relaxed = true)

        val dependencyContainer =
            FakeCoreDependencyContainer(
                concurrentHandlerHolder = concurrentHandlerHolder,
                logger = mockLogger
            )

        setupCoreComponent(dependencyContainer)
    }

    @After
    fun tearDown() {
        tearDownCoreComponent()
    }

    @Test
    fun testInvoke_shouldInvokeMethod() {
        val expected: CharSequence = "test"

        val result = expected.proxyWithLogExceptions()

        result.toString() shouldBe "test"
    }

    @Test
    fun testInvoke_shouldLogException() {

        val callback = Runnable {
            throw Exception("Test")
        }

        callback.proxyWithLogExceptions().run()
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.coreHandler.post {
            latch.countDown()
        }
        latch.await()
        verify { mockLogger.handleLog(LogLevel.ERROR, any(), null) }
    }

    @Test
    fun testInvoke_shouldLogCauseWhenPossible() {
        val expectedCause = RuntimeException("test exception")
        val exception = InvocationTargetException(expectedCause)
        val callback = Runnable {
            throw exception
        }

        callback.proxyWithLogExceptions().run()

        val slot = slot<CrashLog>()
        verify { mockLogger.handleLog(LogLevel.ERROR, capture(slot), null) }
        slot.captured.throwable shouldBe expectedCause
    }
}