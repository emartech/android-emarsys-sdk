package com.emarsys.mobileengage.util

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class RequestUrlUtilsTest {
    private companion object {
        const val APPLICATION_CODE = "app_code"
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net/v3"
        const val CLIENT_BASE = "$CLIENT_HOST/apps/%s/client"
        const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net/v3"
        const val EVENT_HOST_V4 = "https://mobile-events.eservice.emarsys.net/v4"
        const val EVENT_BASE = "$EVENT_HOST/apps/%s/client/events"
        const val EVENT_BASE_V4 = "$EVENT_HOST_V4/apps/%s/client/events"
        const val INBOX_HOST = "https://me-inbox.eservice.emarsys.net/v3"
        const val INBOX_BASE = "$INBOX_HOST/apps/%s/inbox"
        const val REMOTE_CONFIG_HOST = "https://mobile-sdk-config.gservice.emarsys.net"
    }

    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockClientServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceV4Provider: ServiceEndpointProvider
    private lateinit var mockMessageInboxServiceProvider: ServiceEndpointProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
        }
        mockClientServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(CLIENT_HOST)
        }
        mockEventServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(EVENT_HOST)
        }
        mockEventServiceV4Provider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(EVENT_HOST_V4)
        }
        mockMessageInboxServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(INBOX_BASE)
        }
    }

    @Test
    fun testIsMobileEngageUrl_true_whenItIsMobileEngageClient() {
        val result = RequestUrlUtils.isMobileEngageUrl(CLIENT_BASE, mockClientServiceProvider, mockEventServiceProvider, mockEventServiceV4Provider, mockMessageInboxServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageUrl_true_whenItIsMobileEngageV3Event() {
        val result = RequestUrlUtils.isMobileEngageUrl(EVENT_BASE, mockClientServiceProvider, mockEventServiceProvider, mockEventServiceV4Provider, mockMessageInboxServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageUrl_true_whenItIsMobileEngageV4Event() {
        val result = RequestUrlUtils.isMobileEngageUrl(EVENT_BASE_V4, mockClientServiceProvider, mockEventServiceProvider, mockEventServiceV4Provider, mockMessageInboxServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageUrl_true_whenItIsMobileEngageMessageInbox() {
        val result = RequestUrlUtils.isMobileEngageUrl(INBOX_BASE, mockClientServiceProvider, mockEventServiceProvider, mockEventServiceV4Provider, mockMessageInboxServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageUrl_false_whenItIsNotMobileEngage() {
        val result = RequestUrlUtils.isMobileEngageUrl("https://not-mobile-engage.com", mockClientServiceProvider, mockEventServiceProvider, mockEventServiceV4Provider, mockMessageInboxServiceProvider)

        result shouldBe false
    }

    @Test
    fun testIsRemoteConfig_true_whenItIsRemoteConfigUrl() {
        val result = RequestUrlUtils.isMobileEngageUrl("$REMOTE_CONFIG_HOST/$APPLICATION_CODE", mockClientServiceProvider, mockEventServiceProvider, mockEventServiceV4Provider, mockMessageInboxServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsCustomEvent_false_whenItIsNotCustomEventV3Event() {
        val result = RequestUrlUtils.isCustomEvent(CLIENT_BASE, mockEventServiceProvider, mockEventServiceV4Provider)

        result shouldBe false
    }

    @Test
    fun testIsCustomEvent_true_whenItIsCustomEventV3Event() {
        val result = RequestUrlUtils.isCustomEvent(EVENT_BASE, mockEventServiceProvider, mockEventServiceV4Provider)

        result shouldBe true
    }

    @Test
    fun testIsCustomEvent_true_whenItIsCustomEventV4Event() {
        val result = RequestUrlUtils.isCustomEvent(EVENT_BASE_V4, mockEventServiceProvider, mockEventServiceV4Provider)

        result shouldBe true
    }

    @Test
    fun testIsRefreshContactTokenUrl_shouldBeTrue() {
        val result = RequestUrlUtils.isRefreshContactTokenUrl("https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact-token", mockClientServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsRefreshContactTokenUrl_shouldBeFalse() {
        val result = RequestUrlUtils.isRefreshContactTokenUrl("https://not-refresh-token.com", mockClientServiceProvider)

        result shouldBe false
    }
}