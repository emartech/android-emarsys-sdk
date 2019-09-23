package com.emarsys.config

import com.emarsys.core.Callable
import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*


class ConfigProxyTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    lateinit var configProxy: ConfigProxy
    lateinit var mockRunnerProxy: RunnerProxy
    lateinit var mockConfigInternal: ConfigInternal
    @Before
    fun setUp() {
        mockRunnerProxy = spy(RunnerProxy())
        mockConfigInternal = mock(ConfigInternal::class.java)

        configProxy = ConfigProxy(mockRunnerProxy, mockConfigInternal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_runnerProxy_mustNotBeNull() {
        ConfigProxy(null, mockConfigInternal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_configInternal_mustNotBeNull() {
        ConfigProxy(mockRunnerProxy, null)
    }

    @Test
    fun testSetContactFieldId_delegatesTo_internal_byUsingRunnerProxy() {
        configProxy.contactFieldId = 3

        verify(mockConfigInternal).contactFieldId = 3
        verify(mockRunnerProxy).logException(any(Runnable::class.java))
    }

    @Test
    fun testGetContactFieldId_delegatesTo_internal_byUsingRunnerProxy() {
        whenever(mockConfigInternal.contactFieldId).thenReturn(3)

        val result = configProxy.contactFieldId

        result shouldBe 3
        verify(mockConfigInternal).contactFieldId
        verify(mockRunnerProxy).logException(any(Callable::class.java))
    }

    @Test
    fun testChangeApplicationCode_delegatesTo_internal_byUsingRunnerProxy() {
        val mockCompletionListener = mock(CompletionListener::class.java)
        configProxy.changeApplicationCode("testApplicationCode", mockCompletionListener)

        verify(mockConfigInternal).changeApplicationCode("testApplicationCode", mockCompletionListener)
        verify(mockRunnerProxy).logException(any(Runnable::class.java))
    }

    @Test
    fun testGetApplicationCode_delegatesTo_internal_byUsingRunnerProxy() {
        whenever(mockConfigInternal.applicationCode).thenReturn("testApplicationCode")

        val result = configProxy.applicationCode

        result shouldBe "testApplicationCode"
        verify(mockConfigInternal).applicationCode
        verify(mockRunnerProxy).logException(any(Callable::class.java))
    }

    @Test
    fun testChangeMerchantId_delegatesTo_internal_byUsingRunnerProxy() {
        configProxy.changeMerchantId("testMerchantId")

        verify(mockConfigInternal).changeMerchantId("testMerchantId")
        verify(mockRunnerProxy).logException(any(Runnable::class.java))
    }
}