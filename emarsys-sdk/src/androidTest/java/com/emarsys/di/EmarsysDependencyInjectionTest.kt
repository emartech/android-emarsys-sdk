package com.emarsys.di

import android.os.Handler
import android.os.Looper
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.feature.InnerFeature
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictInternal
import com.emarsys.push.PushApi
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class EmarsysDependencyInjectionTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockLoggingMobileEngageInternal: MobileEngageInternal
    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var mockLoggingPredictInternal: PredictInternal

    private lateinit var mockInbox: InboxApi
    private lateinit var mockLoggingInbox: InboxApi
    private lateinit var mockMessageInbox: MessageInboxApi
    private lateinit var mockLoggingMessageInbox: MessageInboxApi

    private lateinit var mockInApp: InAppApi
    private lateinit var mockLoggingInApp: InAppApi
    private lateinit var mockDeepLinkInternal: DeepLinkInternal
    private lateinit var mockLoggingDeepLinkInternal: DeepLinkInternal

    private lateinit var mockClientServiceInternal: ClientServiceInternal
    private lateinit var mockLoggingClientServiceInternal: ClientServiceInternal
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockLoggingEventServiceInternal: EventServiceInternal
    private lateinit var mockPush: PushApi
    private lateinit var mockLoggingPush: PushApi
    private lateinit var mockGeofence: GeofenceApi
    private lateinit var mockLoggingGeofence: GeofenceApi
    private lateinit var mockPredict: PredictApi
    private lateinit var mockLoggingPredict: PredictApi

    @Before
    fun setUp() {
        DependencyInjection.tearDown()

        mockMobileEngageInternal = mock()
        mockLoggingMobileEngageInternal = mock()

        mockInbox = mock()
        mockLoggingInbox = mock()
        mockMessageInbox = mock()
        mockLoggingMessageInbox = mock()

        mockInApp = mock()
        mockLoggingInApp = mock()

        mockDeepLinkInternal = mock()
        mockLoggingDeepLinkInternal = mock()

        mockClientServiceInternal = mock()
        mockLoggingClientServiceInternal = mock()

        mockEventServiceInternal = mock()
        mockLoggingEventServiceInternal = mock()

        mockPush = mock()
        mockLoggingPush = mock()

        mockPredict = mock()
        mockLoggingPredict = mock()

        mockPredictInternal = mock()
        mockLoggingPredictInternal = mock()
        mockGeofence = mock()
        mockLoggingGeofence = mock()

        DependencyInjection.setup(FakeDependencyContainer(
                mobileEngageInternal = mockMobileEngageInternal,
                loggingMobileEngageInternal = mockLoggingMobileEngageInternal,
                deepLinkInternal = mockDeepLinkInternal,
                loggingDeepLinkInternal = mockLoggingDeepLinkInternal,
                eventServiceInternal = mockEventServiceInternal,
                loggingEventServiceInternal = mockLoggingEventServiceInternal,
                clientServiceInternal = mockClientServiceInternal,
                loggingClientServiceInternal = mockLoggingClientServiceInternal,
                predictInternal = mockPredictInternal,
                loggingPredictInternal = mockLoggingPredictInternal,
                inbox = mockInbox,
                loggingInbox = mockLoggingInbox,
                messageInbox = mockMessageInbox,
                loggingMessageInbox = mockLoggingMessageInbox,
                inApp = mockInApp,
                loggingInApp = mockLoggingInApp,
                push = mockPush,
                loggingPush = mockLoggingPush,
                predict = mockPredict,
                loggingPredict = mockLoggingPredict,
                geofence = mockGeofence,
                loggingGeofence = mockLoggingGeofence
        ))
    }

    @After
    fun tearDown() {
        FeatureTestUtils.resetFeatures()
        try {
            val handler = getDependency<Handler>("coreSdkHandler")
            val looper: Looper? = handler.looper
            looper?.quit()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testMobileEngageInternal_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.mobileEngageInternal()
        result shouldBeSameInstanceAs mockMobileEngageInternal
    }

    @Test
    fun testMobileEngageInternal_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.mobileEngageInternal()

        result shouldBeSameInstanceAs mockLoggingMobileEngageInternal
    }

    @Test
    fun testPredictInternal_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.PREDICT)

        val result = EmarsysDependencyInjection.predictInternal()
        result shouldBeSameInstanceAs mockPredictInternal
    }

    @Test
    fun testPredictInternal_shouldReturnLoggingInstance_whenPredictIsDisabled() {
        val result = EmarsysDependencyInjection.predictInternal()

        result shouldBeSameInstanceAs mockLoggingPredictInternal
    }

    @Test
    fun testInbox_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.inbox()
        result shouldBeSameInstanceAs mockInbox
    }

    @Test
    fun testInbox_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.inbox()

        result shouldBeSameInstanceAs mockLoggingInbox
    }

    @Test
    fun testInApp_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.inApp()
        result shouldBeSameInstanceAs mockInApp
    }

    @Test
    fun testInApp_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.inApp()

        result shouldBeSameInstanceAs mockLoggingInApp
    }

    @Test
    fun testDeepLinkInternal_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.deepLinkInternal()
        result shouldBeSameInstanceAs mockDeepLinkInternal
    }

    @Test
    fun testDeepLinkInternal_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.deepLinkInternal()

        result shouldBeSameInstanceAs mockLoggingDeepLinkInternal
    }

    @Test
    fun testClientServiceInternal_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.clientServiceInternal()
        result shouldBeSameInstanceAs mockClientServiceInternal
    }

    @Test
    fun testClientServiceInternal_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.clientServiceInternal()

        result shouldBeSameInstanceAs mockLoggingClientServiceInternal
    }

    @Test
    fun testEventServiceInternal_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.eventServiceInternal()
        result shouldBeSameInstanceAs mockEventServiceInternal
    }

    @Test
    fun testEventServiceInternal_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.eventServiceInternal()

        result shouldBeSameInstanceAs mockLoggingEventServiceInternal
    }

    @Test
    fun testPush_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.push()
        result shouldBeSameInstanceAs mockPush
    }

    @Test
    fun testPush_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.push()

        result shouldBeSameInstanceAs mockLoggingPush
    }

    @Test
    fun testGeofence_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.geofence()

        result shouldBeSameInstanceAs mockGeofence
    }

    @Test
    fun testGeofence_shouldReturnLoggingInstance() {
        val result = EmarsysDependencyInjection.geofence()

        result shouldBeSameInstanceAs mockLoggingGeofence
    }

    @Test
    fun testPredict_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.PREDICT)

        val result = EmarsysDependencyInjection.predict()
        result shouldBeSameInstanceAs mockPredict
    }

    @Test
    fun testPredict_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.predict()

        result shouldBeSameInstanceAs mockLoggingPredict
    }

    @Test
    fun testMessageInbox_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.messageInbox()
        result shouldBeSameInstanceAs mockMessageInbox
    }

    @Test
    fun testMessageInbox_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.messageInbox()

        result shouldBeSameInstanceAs mockLoggingMessageInbox
    }
}