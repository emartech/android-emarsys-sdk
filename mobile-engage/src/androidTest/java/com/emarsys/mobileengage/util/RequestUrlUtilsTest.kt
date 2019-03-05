package com.emarsys.mobileengage.util

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.RequestContext
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.net.URL

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
        RequestUrlUtils.createTrackCustomEventUrl(null)
    }

    @Test
    fun testCreateTrackCustomEventUrl() {
        val url = RequestUrlUtils.createTrackCustomEventUrl(requestContextMock)

        url shouldBe "https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/events"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsMobileEngageRequest_requestModel_mustNotBeNull() {
        RequestUrlUtils.isMobileEngageRequest(null)
    }

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageClient() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_CLIENT_BASE))
        }
        val result = RequestUrlUtils.isMobileEngageRequest(mockRequestModel)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageEvent() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_EVENT_BASE))
        }
        val result = RequestUrlUtils.isMobileEngageRequest(mockRequestModel)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_false_whenItIsNotMobileEngage() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL("https://not-mobile-engage.com"))
        }
        val result = RequestUrlUtils.isMobileEngageRequest(mockRequestModel)

        result shouldBe false
    }
}