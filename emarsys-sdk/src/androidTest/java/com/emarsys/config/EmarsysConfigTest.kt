package com.emarsys.config

import android.app.Application
import com.emarsys.core.api.experimental.FlipperFeature
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.fail
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class EmarsysConfigTest {
    companion object {
        private const val APP_ID = "appID"
        private const val CONTACT_FIELD_ID = 567
        private const val MERCHANT_ID = "MERCHANT_ID"
        private const val SHARED_SECRET = "testSecret"
        private const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
        private const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        private const val INBOX_HOST = "https://me-inbox.eservice.emarsys.net/v3"
        private val SHARED_PACKAGE_NAMES = listOf("package1", "package2")
    }

    private lateinit var application: Application
    private lateinit var defaultInAppEventHandler: EventHandler
    private lateinit var defaultNotificationEventHandler: EventHandler
    private lateinit var features: Array<FlipperFeature>
    private var automaticPushTokenSending = false

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        FeatureTestUtils.resetFeatures()
        val mockClientServiceProvider: ServiceEndpointProvider = mock {
            on { provideEndpointHost() } doReturn CLIENT_HOST
        }
        val mockEventServiceProvider: ServiceEndpointProvider = mock {
            on { provideEndpointHost() } doReturn EVENT_HOST
        }
        val mockMessageInboxServiceProvider: ServiceEndpointProvider = mock {
            on { provideEndpointHost() } doReturn INBOX_HOST
        }
        val dependencyContainer = FakeDependencyContainer(
                clientServiceProvider = mockClientServiceProvider,
                eventServiceProvider = mockEventServiceProvider,
                messageInboxServiceProvider = mockMessageInboxServiceProvider
        )

        DependencyInjection.setup(dependencyContainer)


        automaticPushTokenSending = true
        application = getTargetContext().applicationContext as Application
        defaultInAppEventHandler = mock()
        defaultNotificationEventHandler = mock()
        features = arrayOf(
                mock(),
                mock()
        )
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys(application)
    }

    @Test
    fun testBuilder_withAllArguments() {
        val expected = EmarsysConfig(
                application,
                APP_ID,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                listOf(*features),
                automaticPushTokenSending,
                SHARED_PACKAGE_NAMES,
                SHARED_SECRET,
                true)
        val result = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .enableExperimentalFeatures(*features)
                .inAppEventHandler(mock())
                .notificationEventHandler(mock())
                .sharedSecret("testSecret")
                .sharedPackageNames(SHARED_PACKAGE_NAMES)
                .enableVerboseConsoleLogging()
                .build()
        result.application shouldBe expected.application
        result.contactFieldId shouldBe expected.contactFieldId
        result.experimentalFeatures shouldBe expected.experimentalFeatures
        result.mobileEngageApplicationCode shouldBe expected.mobileEngageApplicationCode
        result.predictMerchantId shouldBe expected.predictMerchantId
        result.sharedSecret shouldBe expected.sharedSecret
        result.sharedPackageNames shouldBe expected.sharedPackageNames
        result.verboseConsoleLoggingEnabled shouldBe expected.verboseConsoleLoggingEnabled

        result.inAppEventHandler?.javaClass?.isInstance(expected.inAppEventHandler) shouldBe true
        result.notificationEventHandler?.javaClass?.isInstance(expected.notificationEventHandler) shouldBe true
    }

    @Test
    fun testBuilder_withRequiredArguments() {
        val expected = EmarsysConfig(
                application,
                APP_ID,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                null,
                null,
                emptyList(),
                automaticPushTokenSending,
                null,
                null,
                false)
        val result = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .build()

        result shouldBe expected
    }

    @Test
    fun testBuilder_whenInAppMessagingFlipperIsOff_defaultInAppMessageHandlerIsNotRequired() {
        try {
            EmarsysConfig.Builder()
                    .application(application)
                    .mobileEngageApplicationCode(APP_ID)
                    .contactFieldId(CONTACT_FIELD_ID)
                    .predictMerchantId(MERCHANT_ID)
                    .build()
        } catch (e: IllegalArgumentException) {
            fail("Should not fail with: ${e.message}")
        }
    }

    @Test
    fun testBuilder_automaticPushTokenSending_whenDisabled() {
        val config = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .disableAutomaticPushTokenSending()
                .build()
        config.automaticPushTokenSendingEnabled shouldBe false
    }

    @Test
    fun testBuilder_automaticPushTokenSending_default() {
        val config = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .build()

        config.automaticPushTokenSendingEnabled shouldBe true
    }

    @Test
    fun testBuilder_verboseConsoleLoggingEnabled() {
        val config = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .enableVerboseConsoleLogging()
                .build()

        config.verboseConsoleLoggingEnabled shouldBe true
    }

    @Test
    fun testBuilder_verboseConsoleLoggingDisabled_byDefault() {
        val config = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .build()

        config.verboseConsoleLoggingEnabled shouldBe false
    }

    @Test
    fun testBuilder_from() {
        val expected = EmarsysConfig(
                application,
                APP_ID,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                listOf(*features),
                automaticPushTokenSending,
                SHARED_PACKAGE_NAMES,
                SHARED_SECRET,
                false)
        val result = EmarsysConfig.Builder()
                .from(expected)
                .build()
        result.application shouldBe expected.application
        result.contactFieldId shouldBe expected.contactFieldId
        result.experimentalFeatures shouldBe expected.experimentalFeatures
        result.mobileEngageApplicationCode shouldBe expected.mobileEngageApplicationCode
        result.predictMerchantId shouldBe expected.predictMerchantId
        result.sharedSecret shouldBe expected.sharedSecret
        result.sharedPackageNames shouldBe expected.sharedPackageNames
        result.verboseConsoleLoggingEnabled shouldBe false

        result.inAppEventHandler?.javaClass?.isInstance(expected.inAppEventHandler) shouldBe true
        result.notificationEventHandler?.javaClass?.isInstance(expected.notificationEventHandler) shouldBe true
    }
}