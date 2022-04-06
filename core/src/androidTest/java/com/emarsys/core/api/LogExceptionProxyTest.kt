package com.emarsys.core.api

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.di.FakeCoreDependencyContainer
import com.emarsys.core.di.setupCoreComponent
import com.emarsys.core.di.tearDownCoreComponent
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.log.LogLevel
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CountDownLatch

class LogExceptionProxyTest {
    private lateinit var mockLogger: Logger
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockLogger = mock()

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
        verify(mockLogger).handleLog(eq(LogLevel.ERROR), any(), eq(null))
    }

    @Test
    fun testInvoke_shouldLogCauseWhenPossible() {
        val expectedCause = RuntimeException("test exception")
        val exception = InvocationTargetException(expectedCause)
        val callback = Runnable {
            throw exception
        }

        callback.proxyWithLogExceptions().run()

        argumentCaptor<CrashLog> {
            verify(mockLogger).handleLog(eq(LogLevel.ERROR), capture(), eq(null))
            firstValue.throwable shouldBe expectedCause
        }
    }
}