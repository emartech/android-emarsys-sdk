package com.emarsys.core.api

import com.emarsys.core.RunnerProxy
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.util.log.LogLevel
import com.emarsys.core.util.log.Logger
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class LogExceptionProxyTest {
    private lateinit var mockLogger: Logger

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockLogger = mock()

        val dependencyContainer = mock<DependencyContainer>().apply {
            whenever(logger).thenReturn(mockLogger)
        }

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

        verify(mockLogger).persistLog(eq(LogLevel.ERROR), any(), eq(null))
    }
}