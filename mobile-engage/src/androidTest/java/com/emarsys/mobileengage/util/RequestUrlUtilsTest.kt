package com.emarsys.mobileengage.util

import com.emarsys.mobileengage.RequestContext
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class RequestUrlUtilsTest {
    companion object {
        const val APPLICATION_CODE = "app_code"
    }

    lateinit var requestContextMock: RequestContext

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        requestContextMock = Mockito.mock(RequestContext::class.java).apply {
            MockitoTestUtils.whenever(applicationCode).thenReturn(APPLICATION_CODE)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenUrl_requestContext_mustNotBeNull() {
        RequestUrlUtils.createSetPushTokenUrl(null)
    }

    @Test
    fun testCreateSetPushTokenUrl() {
        val url = RequestUrlUtils.createSetPushTokenUrl(requestContextMock)
        url shouldBe "https://ems-me-client.herokuapp.com/v3/apps/$APPLICATION_CODE/client/push-token"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateTrackDeviceInfoUrl_requestContext_mustNotBeNull() {
        RequestUrlUtils.createTrackDeviceInfoUrl(null)
    }

    @Test
    fun testCreateTrackDeviceInfoUrl() {
        val url = RequestUrlUtils.createTrackDeviceInfoUrl(requestContextMock)
        url shouldBe "https://ems-me-client.herokuapp.com/v3/apps/$APPLICATION_CODE/client"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetContactUrl_requestContext_mustNotBeNull() {
        RequestUrlUtils.createSetContactUrl(null)
    }

    @Test
    fun testCreateSetContactUrl() {
        val url = RequestUrlUtils.createSetContactUrl(requestContextMock)

        url shouldBe "https://ems-me-client.herokuapp.com/v3/apps/$APPLICATION_CODE/client/contact"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateTrackCustomEventUrl_requestContext_mustNotBeNull() {
        RequestUrlUtils.createCustomEventUrl(null)
    }

    @Test
    fun testCreateTrackCustomEventUrl() {
        val url = RequestUrlUtils.createCustomEventUrl(requestContextMock)

        url shouldBe "https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/events"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsMobileEngageUrl_url_mustNotBeNull() {
        RequestUrlUtils.isMobileEngageUrl(null)
    }

    @Test
    fun testIsMobileEngageUrl_true_whenItIsMobileEngageClient() {
        val result = RequestUrlUtils.isMobileEngageUrl(Endpoint.ME_V3_CLIENT_BASE)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageUrl_true_whenItIsMobileEngageEvent() {
        val result = RequestUrlUtils.isMobileEngageUrl(Endpoint.ME_V3_EVENT_BASE)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageUrl_false_whenItIsNotMobileEngage() {
        val result = RequestUrlUtils.isMobileEngageUrl("https://not-mobile-engage.com")

        result shouldBe false
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsCustomEvent_V3_requestModel_mustNotBeNull() {
        RequestUrlUtils.isCustomEvent_V3(null)
    }

    @Test
    fun testIsCustomEvent_V3_true_whenItIsCustomEventV3Event() {
        val result = RequestUrlUtils.isCustomEvent_V3(Endpoint.ME_V3_EVENT_BASE)

        result shouldBe true
    }

    @Test
    fun testIsCustomEvent_V3_false_whenItIsNotCustomEventV3Event() {
        val result = RequestUrlUtils.isCustomEvent_V3(Endpoint.ME_V3_CLIENT_BASE)

        result shouldBe false
    }
}