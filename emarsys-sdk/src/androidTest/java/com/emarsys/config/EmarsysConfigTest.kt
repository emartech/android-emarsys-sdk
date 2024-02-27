package com.emarsys.config


import android.app.Application
import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.api.experimental.FlipperFeature
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.IntegrationTestUtils
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class EmarsysConfigTest : AnnotationSpec() {
    companion object {
        private const val APP_ID = "appID"
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
            clientServiceEndpointProvider = mockClientServiceProvider,
            eventServiceEndpointProvider = mockEventServiceProvider,
            messageInboxServiceProvider = mockMessageInboxServiceProvider
        )

        setupEmarsysComponent(dependencyContainer)


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
            MERCHANT_ID,
            listOf(*features),
            automaticPushTokenSending,
            SHARED_PACKAGE_NAMES,
            SHARED_SECRET,
            true
        )
        val result = EmarsysConfig.Builder()
            .application(application)
            .applicationCode(APP_ID)
            .merchantId(MERCHANT_ID)
            .enableExperimentalFeatures(*features)
            .sharedSecret("testSecret")
            .sharedPackageNames(SHARED_PACKAGE_NAMES)
            .enableVerboseConsoleLogging()
            .build()
        result.application shouldBe expected.application
        result.experimentalFeatures shouldBe expected.experimentalFeatures
        result.applicationCode shouldBe expected.applicationCode
        result.merchantId shouldBe expected.merchantId
        result.sharedSecret shouldBe expected.sharedSecret
        result.sharedPackageNames shouldBe expected.sharedPackageNames
        result.verboseConsoleLoggingEnabled shouldBe expected.verboseConsoleLoggingEnabled
    }

    @Test
    fun testBuilder_withRequiredArguments() {
        val expected = EmarsysConfig(
            application,
            APP_ID,
            MERCHANT_ID,
            emptyList(),
            automaticPushTokenSending,
            null,
            null,
            false
        )
        val result = EmarsysConfig.Builder()
            .application(application)
            .applicationCode(APP_ID)
            .merchantId(MERCHANT_ID)
            .build()

        result shouldBe expected
    }

    @Test
    fun testBuilder_whenInAppMessagingFlipperIsOff_defaultInAppMessageHandlerIsNotRequired() {
        try {
            EmarsysConfig.Builder()
                .application(application)
                .applicationCode(APP_ID)
                .merchantId(MERCHANT_ID)
                .build()
        } catch (e: IllegalArgumentException) {
            fail("Should not fail with: ${e.message}")
        }
    }

    @Test
    fun testBuilder_automaticPushTokenSending_whenDisabled() {
        val config = EmarsysConfig.Builder()
            .application(application)
            .applicationCode(APP_ID)
            .merchantId(MERCHANT_ID)
            .disableAutomaticPushTokenSending()
            .build()
        config.automaticPushTokenSendingEnabled shouldBe false
    }

    @Test
    fun testBuilder_automaticPushTokenSending_default() {
        val config = EmarsysConfig.Builder()
            .application(application)
            .applicationCode(APP_ID)
            .merchantId(MERCHANT_ID)
            .build()

        config.automaticPushTokenSendingEnabled shouldBe true
    }

    @Test
    fun testBuilder_verboseConsoleLoggingEnabled() {
        val config = EmarsysConfig.Builder()
            .application(application)
            .applicationCode(APP_ID)
            .merchantId(MERCHANT_ID)
            .enableVerboseConsoleLogging()
            .build()

        config.verboseConsoleLoggingEnabled shouldBe true
    }

    @Test
    fun testBuilder_verboseConsoleLoggingDisabled_byDefault() {
        val config = EmarsysConfig.Builder()
            .application(application)
            .applicationCode(APP_ID)
            .merchantId(MERCHANT_ID)
            .build()

        config.verboseConsoleLoggingEnabled shouldBe false
    }

    @Test
    fun testEmarsysConfig_defaultParameters() {
        val config = EmarsysConfig(
            application = application
        )

        config.application shouldBe application
        config.applicationCode shouldBe null
        config.automaticPushTokenSendingEnabled shouldBe true
        config.verboseConsoleLoggingEnabled shouldBe false
        config.experimentalFeatures shouldBe listOf()
        config.merchantId shouldBe null
        config.sharedSecret shouldBe null
        config.sharedPackageNames shouldBe null
    }

    @Test
    fun testEmarsysConfig_allParameters() {
        val config = EmarsysConfig(
            application = application,
            applicationCode = APP_ID,
            merchantId = MERCHANT_ID,
            experimentalFeatures = listOf(InnerFeature.EVENT_SERVICE_V4),
            automaticPushTokenSendingEnabled = false,
            sharedPackageNames = SHARED_PACKAGE_NAMES,
            sharedSecret = SHARED_SECRET,
            verboseConsoleLoggingEnabled = true
        )

        config.application shouldBe application
        config.applicationCode shouldBe APP_ID
        config.automaticPushTokenSendingEnabled shouldBe false
        config.verboseConsoleLoggingEnabled shouldBe true
        config.experimentalFeatures shouldBe listOf(InnerFeature.EVENT_SERVICE_V4)
        config.merchantId shouldBe MERCHANT_ID
        config.sharedSecret shouldBe SHARED_SECRET
        config.sharedPackageNames shouldBe SHARED_PACKAGE_NAMES
    }

    @Test
    fun testBuilder_from() {
        val expected = EmarsysConfig(
            application,
            APP_ID,
            MERCHANT_ID,
            listOf(*features),
            automaticPushTokenSending,
            SHARED_PACKAGE_NAMES,
            SHARED_SECRET,
            false
        )
        val result = EmarsysConfig.Builder()
            .from(expected)
            .build()
        result.application shouldBe expected.application
        result.experimentalFeatures shouldBe expected.experimentalFeatures
        result.applicationCode shouldBe expected.applicationCode
        result.merchantId shouldBe expected.merchantId
        result.sharedSecret shouldBe expected.sharedSecret
        result.sharedPackageNames shouldBe expected.sharedPackageNames
        result.verboseConsoleLoggingEnabled shouldBe false
    }
}