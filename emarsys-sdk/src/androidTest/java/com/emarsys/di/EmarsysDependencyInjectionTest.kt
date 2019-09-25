package com.emarsys.di

import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.feature.InnerFeature
import com.emarsys.inapp.InAppApi
import com.emarsys.inbox.InboxApi
import com.emarsys.mobileengage.DefaultMobileEngageInternal
import com.emarsys.mobileengage.LoggingMobileEngageInternal
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.client.DefaultClientServiceInternal
import com.emarsys.mobileengage.client.LoggingClientServiceInternal
import com.emarsys.mobileengage.deeplink.DeepLinkInternal
import com.emarsys.mobileengage.deeplink.DefaultDeepLinkInternal
import com.emarsys.mobileengage.deeplink.LoggingDeepLinkInternal
import com.emarsys.mobileengage.event.DefaultEventServiceInternal
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.event.LoggingEventServiceInternal
import com.emarsys.predict.PredictApi
import com.emarsys.predict.PredictInternal
import com.emarsys.push.PushApi
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.matchers.types.shouldBeSameInstanceAs

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

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
    private lateinit var mockPredict: PredictApi
    private lateinit var mockLoggingPredict: PredictApi


    @Before
    fun setUp() {
        mockMobileEngageInternal = mock(DefaultMobileEngageInternal::class.java)
        mockLoggingMobileEngageInternal = mock(LoggingMobileEngageInternal::class.java)

        mockInbox = mock(InboxApi::class.java)
        mockLoggingInbox = mock(InboxApi::class.java)

        mockInApp = mock(InAppApi::class.java)
        mockLoggingInApp = mock(InAppApi::class.java)

        mockDeepLinkInternal = mock(DefaultDeepLinkInternal::class.java)
        mockLoggingDeepLinkInternal = mock(LoggingDeepLinkInternal::class.java)

        mockClientServiceInternal = mock(DefaultClientServiceInternal::class.java)
        mockLoggingClientServiceInternal = mock(LoggingClientServiceInternal::class.java)

        mockEventServiceInternal = mock(DefaultEventServiceInternal::class.java)
        mockLoggingEventServiceInternal = mock(LoggingEventServiceInternal::class.java)

        mockPush = mock(PushApi::class.java)
        mockLoggingPush = mock(PushApi::class.java)

        mockPredict = mock(PredictApi::class.java)
        mockLoggingPredict = mock(PredictApi::class.java)

        mockPredictInternal = mock(PredictInternal::class.java)
        mockLoggingPredictInternal = mock(PredictInternal::class.java)

        DependencyInjection.setup(FakeDependencyContainer(
                mobileEngageInternal = mockMobileEngageInternal,
                loggingMobileEngageInternal = mockLoggingMobileEngageInternal,
                inbox = mockInbox,
                loggingInbox = mockLoggingInbox,
                inApp = mockInApp,
                loggingInApp = mockLoggingInApp,
                deepLinkInternal = mockDeepLinkInternal,
                loggingDeepLinkInternal = mockLoggingDeepLinkInternal,
                clientServiceInternal = mockClientServiceInternal,
                loggingClientServiceInternal = mockLoggingClientServiceInternal,
                eventServiceInternal = mockEventServiceInternal,
                loggingEventServiceInternal = mockLoggingEventServiceInternal,
                push = mockPush,
                loggingPush = mockLoggingPush,
                predict = mockPredict,
                predictInternal = mockPredictInternal,
                loggingPredictInternal = mockLoggingPredictInternal,
                loggingPredict = mockLoggingPredict
        ))
    }

    @After
    fun tearDown() {
        FeatureTestUtils.resetFeatures()
        DependencyInjection.tearDown()
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
}