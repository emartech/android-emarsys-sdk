package com.emarsys.core.api

import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.di.FakeCoreDependencyContainer
import com.emarsys.core.di.setupCoreComponent
import com.emarsys.core.di.tearDownCoreComponent
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.util.log.LogLevel
import com.emarsys.core.util.log.Logger
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch

class LogExceptionProxyTest {
    private lateinit var mockLogger: Logger
    private lateinit var coreSdkHandler: CoreSdkHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        mockLogger = mock()

        val dependencyContainer = FakeCoreDependencyContainer(coreSdkHandler = coreSdkHandler, logger = mockLogger)

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
        coreSdkHandler.post {
            latch.countDown()
        }
        latch.await()
        verify(mockLogger).handleLog(eq(LogLevel.ERROR), any(), eq(null))
    }
}