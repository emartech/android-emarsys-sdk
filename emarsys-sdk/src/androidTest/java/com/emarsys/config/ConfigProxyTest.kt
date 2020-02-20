package com.emarsys.config

import com.emarsys.core.Callable
import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.notification.NotificationSettings
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
        whenever(mockConfigInternal.contactFieldId).thenReturn(3)

        configProxy.changeApplicationCode("testApplicationCode", mockCompletionListener)

        verify(mockConfigInternal).changeApplicationCode("testApplicationCode", 3, mockCompletionListener)
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

    @Test
    fun testGetMerchantId_delegatesTo_internal_byUsingRunnerProxy() {
        whenever(mockConfigInternal.merchantId).thenReturn("testMerchantId")

        val result = configProxy.merchantId

        result shouldBe "testMerchantId"
        verify(mockConfigInternal).merchantId
        verify(mockRunnerProxy).logException(any(Callable::class.java))
    }

    @Test
    fun testGetNotificationSettings_delegatesTo_internal_byUsingRunnerProxy() {
        val mockNotificationSettings = mock(NotificationSettings::class.java)
        whenever(mockConfigInternal.notificationSettings).thenReturn(mockNotificationSettings)

        val result = configProxy.notificationSettings

        result shouldBe mockNotificationSettings
        verify(mockConfigInternal).notificationSettings
        verify(mockRunnerProxy).logException(any(Callable::class.java))
    }

    @Test
    fun testGetLanguage_delegatesTo_internal_byUsingRunnerProxy() {
        val language = "testLanguage"
        whenever(mockConfigInternal.language).thenReturn(language)

        val result = configProxy.language

        result shouldBe language
        verify(mockConfigInternal).language
        verify(mockRunnerProxy).logException(any(Callable::class.java))
    }

    @Test
    fun testGetHardwareId_delegatesTo_internal_byUsingRunnerProxy() {
        val hardwareId = "testHardwareId"
        whenever(mockConfigInternal.hardwareId).thenReturn(hardwareId)

        val result = configProxy.hardwareId

        result shouldBe hardwareId
        verify(mockConfigInternal).hardwareId
        verify(mockRunnerProxy).logException(any(Callable::class.java))
    }
}