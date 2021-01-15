package com.emarsys.config

import android.app.Application
import com.emarsys.core.api.experimental.FlipperFeature
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.fail
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class EmarsysConfigTest {
    companion object {
        private const val APP_ID = "appID"
        private const val CONTACT_FIELD_ID = 567
        private const val MERCHANT_ID = "MERCHANT_ID"
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
        automaticPushTokenSending = true
        application = getTargetContext().applicationContext as Application
        defaultInAppEventHandler = mock()
        defaultNotificationEventHandler = mock()
        features = arrayOf(
                mock(),
                mock()
        )
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
                null)
        val result = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .enableExperimentalFeatures(*features)
                .inAppEventHandler(mock())
                .notificationEventHandler(mock())
                .build()
        result.application shouldBe expected.application
        result.contactFieldId shouldBe expected.contactFieldId
        result.experimentalFeatures shouldBe expected.experimentalFeatures
        result.mobileEngageApplicationCode shouldBe expected.mobileEngageApplicationCode
        result.predictMerchantId shouldBe expected.predictMerchantId

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
                null)
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
                null)
        val result = EmarsysConfig.Builder()
                .from(expected)
                .build()
        result.application shouldBe expected.application
        result.contactFieldId shouldBe expected.contactFieldId
        result.experimentalFeatures shouldBe expected.experimentalFeatures
        result.mobileEngageApplicationCode shouldBe expected.mobileEngageApplicationCode
        result.predictMerchantId shouldBe expected.predictMerchantId

        result.inAppEventHandler?.javaClass?.isInstance(expected.inAppEventHandler) shouldBe true
        result.notificationEventHandler?.javaClass?.isInstance(expected.notificationEventHandler) shouldBe true
    }
}