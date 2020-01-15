package com.emarsys.core

import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.util.log.LogLevel
import com.emarsys.core.util.log.Logger
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.verify

class RunnerProxyTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var runnerProxy: RunnerProxy
    private lateinit var mockLogger: Logger

    @Before
    fun setUp() {
        mockLogger = mock()

        val dependencyContainer = mock<DependencyContainer>().apply {
            whenever(logger).thenReturn(mockLogger)
        }

        DependencyInjection.setup(dependencyContainer)
        runnerProxy = RunnerProxy()
    }

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
    }

    @Test
    fun testLogException_shouldDelegateToRunnable() {
        val runnable = mock<Runnable>()

        runnerProxy.logException(runnable)

        verify(runnable).run()
    }

    @Test
    fun testLogException_shouldDelegateToCallable() {
        val callable = mock<Callable<Any>>()

        runnerProxy.logException(callable)

        verify(callable).call()
    }

    @Test
    fun testLogException_shouldLogCrashes() {
        val runnable = mock<Runnable>().apply {
            whenever(run()).thenThrow(RuntimeException())
        }
        try {
            runnerProxy.logException(runnable)
        } catch (exception: RuntimeException) {

        }
        verify(mockLogger).persistLog(eq(LogLevel.ERROR), any())
    }
}