package com.emarsys.config

import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.doReturn


class ConfigTest {

    companion object {
        private const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
        private const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        private const val INBOX_HOST = "https://me-inbox.eservice.emarsys.net/v3"
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    lateinit var config: Config
    lateinit var mockConfigInternal: ConfigInternal

    @Before
    fun setUp() {
        mockConfigInternal = mock(ConfigInternal::class.java)
        val mockClientServiceProvider: ServiceEndpointProvider = org.mockito.kotlin.mock {
            on { provideEndpointHost() } doReturn CLIENT_HOST
        }
        val mockEventServiceProvider: ServiceEndpointProvider = org.mockito.kotlin.mock {
            on { provideEndpointHost() } doReturn EVENT_HOST
        }
        val mockMessageInboxServiceProvider: ServiceEndpointProvider = org.mockito.kotlin.mock {
            on { provideEndpointHost() } doReturn INBOX_HOST
        }
        val dependencyContainer = FakeDependencyContainer(
                configInternal = mockConfigInternal,
                clientServiceEndpointProvider = mockClientServiceProvider,
                eventServiceEndpointProvider = mockEventServiceProvider,
                messageInboxServiceProvider = mockMessageInboxServiceProvider
        )

        setupEmarsysComponent(dependencyContainer)
        config = Config()
    }


    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testGetContactFieldId_delegatesTo_internal() {
        whenever(mockConfigInternal.contactFieldId).thenReturn(3)

        val result = config.contactFieldId

        result shouldBe 3
        verify(mockConfigInternal).contactFieldId
    }

    @Test
    fun testChangeApplicationCode_delegatesTo_internal() {
        val mockCompletionListener = mock(CompletionListener::class.java)
        whenever(mockConfigInternal.contactFieldId).thenReturn(3)

        config.changeApplicationCode("testApplicationCode", mockCompletionListener)

        verify(mockConfigInternal).changeApplicationCode("testApplicationCode", mockCompletionListener)
    }

    @Test
    fun testGetApplicationCode_delegatesTo_internal() {
        whenever(mockConfigInternal.applicationCode).thenReturn("testApplicationCode")

        val result = config.applicationCode

        result shouldBe "testApplicationCode"
        verify(mockConfigInternal).applicationCode
    }

    @Test
    fun testChangeMerchantId_delegatesTo_internal() {
        config.changeMerchantId("testMerchantId")

        verify(mockConfigInternal).changeMerchantId("testMerchantId")
    }

    @Test
    fun testGetMerchantId_delegatesTo_internal() {
        whenever(mockConfigInternal.merchantId).thenReturn("testMerchantId")

        val result = config.merchantId

        result shouldBe "testMerchantId"
        verify(mockConfigInternal).merchantId
    }

    @Test
    fun testGetNotificationSettings_delegatesTo_internal() {
        val mockNotificationSettings = mock(NotificationSettings::class.java)
        whenever(mockConfigInternal.notificationSettings).thenReturn(mockNotificationSettings)

        val result = config.notificationSettings

        result shouldBe mockNotificationSettings
        verify(mockConfigInternal).notificationSettings
    }

    @Test
    fun testGetLanguage_delegatesTo_internal() {
        val language = "testLanguage"
        whenever(mockConfigInternal.language).thenReturn(language)

        val result = config.languageCode

        result shouldBe language
        verify(mockConfigInternal).language
    }

    @Test
    fun testGetHardwareId_delegatesTo_internal() {
        val hardwareId = "testHardwareId"
        whenever(mockConfigInternal.hardwareId).thenReturn(hardwareId)

        val result = config.hardwareId

        result shouldBe hardwareId
        verify(mockConfigInternal).hardwareId
    }

    @Test
    fun testIsAutomaticPushSendingEnabled_delegatesTo_internal() {
        whenever(mockConfigInternal.isAutomaticPushSendingEnabled).thenReturn(true)

        val result = config.isAutomaticPushSendingEnabled

        result shouldBe true
        verify(mockConfigInternal).isAutomaticPushSendingEnabled
    }

    @Test
    fun testSdkVersion_delegatesTo_internal() {
        val sdkVersion = "testSdkVersion"
        whenever(mockConfigInternal.sdkVersion).thenReturn(sdkVersion)

        val result = config.sdkVersion

        result shouldBe sdkVersion
        verify(mockConfigInternal).sdkVersion
    }
}