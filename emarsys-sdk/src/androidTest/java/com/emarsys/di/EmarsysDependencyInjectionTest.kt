package com.emarsys.di

import android.os.Handler
import android.os.Looper
import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.deeplink.DeepLinkApi
import com.emarsys.eventservice.EventServiceApi
import com.emarsys.geofence.GeofenceApi
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.inbox.MessageInboxApi
import com.emarsys.mobileengage.MobileEngageApi
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.oneventaction.OnEventActionApi
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

    private lateinit var mockMobileEngageApi: MobileEngageApi
    private lateinit var mockLoggingMobileEngageApi: MobileEngageApi
    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var mockLoggingPredictInternal: PredictInternal

    private lateinit var mockInbox: InboxApi
    private lateinit var mockLoggingInbox: InboxApi
    private lateinit var mockMessageInbox: MessageInboxApi
    private lateinit var mockLoggingMessageInbox: MessageInboxApi

    private lateinit var mockInApp: InAppApi
    private lateinit var mockLoggingInApp: InAppApi
    private lateinit var mockOnEventAction: OnEventActionApi
    private lateinit var mockLoggingOnEventAction: OnEventActionApi
    private lateinit var mockDeepLinkApi: DeepLinkApi
    private lateinit var mockLoggingDeepLinkApi: DeepLinkApi

    private lateinit var mockClientServiceInternal: ClientServiceInternal
    private lateinit var mockLoggingClientServiceInternal: ClientServiceInternal
    private lateinit var mockEventServiceApi: EventServiceApi
    private lateinit var mockLoggingEventServiceApi: EventServiceApi
    private lateinit var mockPush: PushApi
    private lateinit var mockLoggingPush: PushApi
    private lateinit var mockGeofence: GeofenceApi
    private lateinit var mockLoggingGeofence: GeofenceApi
    private lateinit var mockPredict: PredictApi
    private lateinit var mockLoggingPredict: PredictApi

    @Before
    fun setUp() {
        DependencyInjection.tearDown()

        mockMobileEngageApi = mock()
        mockLoggingMobileEngageApi = mock()

        mockInbox = mock()
        mockLoggingInbox = mock()
        mockMessageInbox = mock()
        mockLoggingMessageInbox = mock()

        mockInApp = mock()
        mockLoggingInApp = mock()

        mockOnEventAction = mock()
        mockLoggingOnEventAction = mock()

        mockDeepLinkApi = mock()
        mockLoggingDeepLinkApi = mock()

        mockClientServiceInternal = mock()
        mockLoggingClientServiceInternal = mock()

        mockEventServiceApi = mock()
        mockLoggingEventServiceApi = mock()

        mockPush = mock()
        mockLoggingPush = mock()

        mockPredict = mock()
        mockLoggingPredict = mock()

        mockPredictInternal = mock()
        mockLoggingPredictInternal = mock()
        mockGeofence = mock()
        mockLoggingGeofence = mock()

        DependencyInjection.setup(FakeDependencyContainer(
                mobileEngageApi = mockMobileEngageApi,
                loggingMobileEngageApi = mockLoggingMobileEngageApi,
                deepLinkApi = mockDeepLinkApi,
                loggingDeepLinkApi = mockLoggingDeepLinkApi,
                eventServiceApi = mockEventServiceApi,
                loggingEventServiceApi = mockLoggingEventServiceApi,
                clientServiceInternal = mockClientServiceInternal,
                loggingClientServiceInternal = mockLoggingClientServiceInternal,
                predictInternal = mockPredictInternal,
                loggingPredictInternal = mockLoggingPredictInternal,
                inbox = mockInbox,
                loggingInbox = mockLoggingInbox,
                messageInbox = mockMessageInbox,
                loggingMessageInbox = mockLoggingMessageInbox,
                onEventAction = mockOnEventAction,
                loggingOnEventAction = mockLoggingOnEventAction,
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

        val result = EmarsysDependencyInjection.mobileEngageApi()
        result shouldBeSameInstanceAs mockMobileEngageApi
    }

    @Test
    fun testMobileEngageInternal_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.mobileEngageApi()

        result shouldBeSameInstanceAs mockLoggingMobileEngageApi
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
    fun testOnEventAction_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.onEventAction()
        result shouldBeSameInstanceAs mockOnEventAction
    }

    @Test
    fun testOnEventAction_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.onEventAction()

        result shouldBeSameInstanceAs mockLoggingOnEventAction
    }

    @Test
    fun testDeepLinkInternal_shouldReturnDefaultInstance() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val result = EmarsysDependencyInjection.deepLinkApi()
        result shouldBeSameInstanceAs mockDeepLinkApi
    }

    @Test
    fun testDeepLinkInternal_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.deepLinkApi()

        result shouldBeSameInstanceAs mockLoggingDeepLinkApi
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

        val result = EmarsysDependencyInjection.eventServiceApi()
        result shouldBeSameInstanceAs mockEventServiceApi
    }

    @Test
    fun testEventServiceInternal_shouldReturnLoggingInstance_whenMEIsDisabled() {
        val result = EmarsysDependencyInjection.eventServiceApi()

        result shouldBeSameInstanceAs mockLoggingEventServiceApi
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