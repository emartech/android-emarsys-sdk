package com.emarsys.core.api

import android.os.Handler
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.FakeCoreDependencyContainer
import com.emarsys.core.util.log.LogLevel
import com.emarsys.core.util.log.Logger
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch

class LogExceptionProxyTest {
    private lateinit var mockLogger: Logger
    private lateinit var coreSdkHandler: Handler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        mockLogger = mock()

        val dependencyContainer = FakeCoreDependencyContainer(coreSdkHandler = coreSdkHandler, logger = mockLogger)

        DependencyInjection.setup(dependencyContainer)
    }

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
    }

    @Test
    fun testProxyWithHandler() {
        val expected: CharSequence = "test"

        val result = expected.proxyWithLogExceptions()

        (result is CharSequence) shouldBe true
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
        verify(mockLogger).persistLog(eq(LogLevel.ERROR), any(), eq(null))
    }
}