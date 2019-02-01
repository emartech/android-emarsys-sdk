package com.emarsys.core

import com.emarsys.testUtil.TimeoutUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class RunnerProxyTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    lateinit var runnerProxy: RunnerProxy

    @Before
    fun setUp() {
        runnerProxy = RunnerProxy()
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
}