package com.emarsys.config


import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule


class ConfigTest : AnnotationSpec() {
    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    companion object {
        private const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
        private const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        private const val INBOX_HOST = "https://me-inbox.eservice.emarsys.net/v3"
    }

    lateinit var config: Config
    private lateinit var mockConfigInternal: ConfigInternal

    @Before
    fun setUp() {
        mockConfigInternal = mockk(relaxed = true)
        val mockClientServiceProvider: ServiceEndpointProvider = mockk(relaxed = true)
        every { mockClientServiceProvider.provideEndpointHost() } returns CLIENT_HOST

        val mockEventServiceProvider: ServiceEndpointProvider = mockk(relaxed = true)
        every { mockEventServiceProvider.provideEndpointHost() } returns EVENT_HOST

        val mockMessageInboxServiceProvider: ServiceEndpointProvider = mockk(relaxed = true)
        every { mockMessageInboxServiceProvider.provideEndpointHost() } returns INBOX_HOST

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
        every { mockConfigInternal.contactFieldId } returns 3

        val result = config.contactFieldId

        result shouldBe 3
        verify { mockConfigInternal.contactFieldId }
    }

    @Test
    fun testChangeApplicationCode_delegatesTo_internal() {
        val mockCompletionListener: CompletionListener = mockk(relaxed = true)
        every { mockConfigInternal.contactFieldId } returns 3

        config.changeApplicationCode("testApplicationCode", mockCompletionListener)

        verify {
            mockConfigInternal.changeApplicationCode(
                "testApplicationCode",
                mockCompletionListener
            )
        }
    }

    @Test
    fun testGetApplicationCode_delegatesTo_internal() {
        every { mockConfigInternal.applicationCode } returns "testApplicationCode"

        val result = config.applicationCode

        result shouldBe "testApplicationCode"
        verify { mockConfigInternal.applicationCode }
    }

    @Test
    fun testChangeMerchantId_delegatesTo_internal() {
        config.changeMerchantId("testMerchantId")

        verify { mockConfigInternal.changeMerchantId("testMerchantId") }
    }

    @Test
    fun testGetMerchantId_delegatesTo_internal() {
        every { mockConfigInternal.merchantId } returns "testMerchantId"

        val result = config.merchantId

        result shouldBe "testMerchantId"
        verify { mockConfigInternal.merchantId }
    }

    @Test
    fun testGetNotificationSettings_delegatesTo_internal() {
        val mockNotificationSettings: NotificationSettings = mockk(relaxed = true)
        every { mockConfigInternal.notificationSettings } returns mockNotificationSettings

        val result = config.notificationSettings

        result shouldBe mockNotificationSettings
        verify { mockConfigInternal.notificationSettings }
    }

    @Test
    fun testGetLanguage_delegatesTo_internal() {
        val language = "testLanguage"
        every { mockConfigInternal.language } returns language

        val result = config.languageCode

        result shouldBe language
        verify { mockConfigInternal.language }
    }

    @Test
    fun testGetClientId_delegatesTo_internal() {
        val clientId = "testClientId"
        every { mockConfigInternal.clientId } returns clientId

        val result = config.hardwareId

        result shouldBe clientId
        verify { mockConfigInternal.clientId }
    }

    @Test
    fun testIsAutomaticPushSendingEnabled_delegatesTo_internal() {
        every { mockConfigInternal.isAutomaticPushSendingEnabled } returns true

        val result = config.isAutomaticPushSendingEnabled

        result shouldBe true
        verify { mockConfigInternal.isAutomaticPushSendingEnabled }
    }

    @Test
    fun testSdkVersion_delegatesTo_internal() {
        val sdkVersion = "testSdkVersion"
        every { mockConfigInternal.sdkVersion } returns sdkVersion

        val result = config.sdkVersion

        result shouldBe sdkVersion
        verify { mockConfigInternal.sdkVersion }
    }
}