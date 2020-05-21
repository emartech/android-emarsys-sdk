package com.emarsys.config

import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.DependencyInjection
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


class ConfigTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    lateinit var config: Config
    lateinit var mockConfigInternal: ConfigInternal

    @Before
    fun setUp() {
        mockConfigInternal = mock(ConfigInternal::class.java)
        val dependencyContainer = FakeDependencyContainer(configInternal = mockConfigInternal)

        DependencyInjection.setup(dependencyContainer)
        config = Config()
    }

    @Test
    fun testGetContactFieldId_delegatesTo_internal_byUsingRunnerProxy() {
        whenever(mockConfigInternal.contactFieldId).thenReturn(3)

        val result = config.contactFieldId

        result shouldBe 3
        verify(mockConfigInternal).contactFieldId
    }

    @Test
    fun testChangeApplicationCode_delegatesTo_internal_byUsingRunnerProxy() {
        val mockCompletionListener = mock(CompletionListener::class.java)
        whenever(mockConfigInternal.contactFieldId).thenReturn(3)

        config.changeApplicationCode("testApplicationCode", mockCompletionListener)

        verify(mockConfigInternal).changeApplicationCode("testApplicationCode", 3, mockCompletionListener)
    }

    @Test
    fun testGetApplicationCode_delegatesTo_internal_byUsingRunnerProxy() {
        whenever(mockConfigInternal.applicationCode).thenReturn("testApplicationCode")

        val result = config.applicationCode

        result shouldBe "testApplicationCode"
        verify(mockConfigInternal).applicationCode
    }

    @Test
    fun testChangeMerchantId_delegatesTo_internal_byUsingRunnerProxy() {
        config.changeMerchantId("testMerchantId")

        verify(mockConfigInternal).changeMerchantId("testMerchantId")
    }

    @Test
    fun testGetMerchantId_delegatesTo_internal_byUsingRunnerProxy() {
        whenever(mockConfigInternal.merchantId).thenReturn("testMerchantId")

        val result = config.merchantId

        result shouldBe "testMerchantId"
        verify(mockConfigInternal).merchantId
    }

    @Test
    fun testGetNotificationSettings_delegatesTo_internal_byUsingRunnerProxy() {
        val mockNotificationSettings = mock(NotificationSettings::class.java)
        whenever(mockConfigInternal.notificationSettings).thenReturn(mockNotificationSettings)

        val result = config.notificationSettings

        result shouldBe mockNotificationSettings
        verify(mockConfigInternal).notificationSettings
    }

    @Test
    fun testGetLanguage_delegatesTo_internal_byUsingRunnerProxy() {
        val language = "testLanguage"
        whenever(mockConfigInternal.language).thenReturn(language)

        val result = config.language

        result shouldBe language
        verify(mockConfigInternal).language
    }

    @Test
    fun testGetHardwareId_delegatesTo_internal_byUsingRunnerProxy() {
        val hardwareId = "testHardwareId"
        whenever(mockConfigInternal.hardwareId).thenReturn(hardwareId)

        val result = config.hardwareId

        result shouldBe hardwareId
        verify(mockConfigInternal).hardwareId
    }
}