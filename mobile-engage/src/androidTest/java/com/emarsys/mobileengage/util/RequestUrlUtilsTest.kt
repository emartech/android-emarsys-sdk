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
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
        const val CLIENT_BASE = "$CLIENT_HOST/v3/apps/%s/client"
        const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val EVENT_BASE = "$EVENT_HOST/v3/apps/%s/client/events"
    }

    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockClientServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceProvider: ServiceEndpointProvider

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
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsMobileEngageUrl_url_mustNotBeNull() {
        RequestUrlUtils.isMobileEngageV3Url(null, mockClientServiceProvider, mockEventServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsMobileEngageUrl_eventServiceProvider_mustNotBeNull() {
        RequestUrlUtils.isMobileEngageV3Url(EVENT_BASE, mockClientServiceProvider, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsMobileEngageUrl_clientServiceProvider_mustNotBeNull() {
        RequestUrlUtils.isMobileEngageV3Url(EVENT_BASE, null, mockEventServiceProvider)
    }

    @Test
    fun testIsMobileEngageUrl_true_whenItIsMobileEngageClient() {
        val result = RequestUrlUtils.isMobileEngageV3Url(CLIENT_BASE, mockClientServiceProvider, mockEventServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageUrl_true_whenItIsMobileEngageEvent() {
        val result = RequestUrlUtils.isMobileEngageV3Url(EVENT_BASE, mockClientServiceProvider, mockEventServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageUrl_false_whenItIsNotMobileEngage() {
        val result = RequestUrlUtils.isMobileEngageV3Url("https://not-mobile-engage.com", mockClientServiceProvider, mockEventServiceProvider)

        result shouldBe false
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsCustomEvent_V3_requestModel_mustNotBeNull() {
        RequestUrlUtils.isCustomEvent_V3(null, mockEventServiceProvider)
    }

    @Test
    fun testIsCustomEvent_V3_true_whenItIsCustomEventV3Event() {
        val result = RequestUrlUtils.isCustomEvent_V3(EVENT_BASE, mockEventServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsCustomEvent_V3_false_whenItIsNotCustomEventV3Event() {
        val result = RequestUrlUtils.isCustomEvent_V3(CLIENT_BASE, mockEventServiceProvider)

        result shouldBe false
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsRefreshContactTokenUrl_url_mustNotBeNull() {
        RequestUrlUtils.isRefreshContactTokenUrl(null, mockEventServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsRefreshContactTokenUrl_eventServiceProvider_mustNotBeNull() {
        RequestUrlUtils.isRefreshContactTokenUrl(CLIENT_HOST, null)
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