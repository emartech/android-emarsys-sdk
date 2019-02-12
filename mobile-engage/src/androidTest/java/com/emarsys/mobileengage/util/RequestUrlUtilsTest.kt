package com.emarsys.mobileengage.util

import com.emarsys.mobileengage.RequestContext
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
}