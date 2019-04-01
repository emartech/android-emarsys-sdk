package com.emarsys.core

import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.util.log.Logger
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class RunnerProxyTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var runnerProxy: RunnerProxy
    private lateinit var mockLogger: Logger

    @Before
    fun setUp() {
        mockLogger = mock(Logger::class.java)

        val dependencyContainer = mock(DependencyContainer::class.java).apply {
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
        val runnable = mock(Runnable::class.java)

        runnerProxy.logException(runnable)

        verify(runnable).run()
    }

    @Test
    fun testLogException_shouldDelegateToCallable() {
        val callable = mock(Callable::class.java)

        runnerProxy.logException(callable)

        verify(callable).call()
    }

    @Test
    fun testLogException_shouldLogCrashes() {
        val runnable = mock(Runnable::class.java).apply {
            whenever(run()).thenThrow(RuntimeException())
        }
        try {
            runnerProxy.logException(runnable)
        } catch (exception: java.lang.RuntimeException) {

        }
        verify(mockLogger).persistLog(any())
    }
}