package com.emarsys.mobileengage.util

import com.emarsys.mobileengage.RequestContext
import com.emarsys.testUtil.mockito.MockitoTestUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class RequestUrlUtilsTest {
    companion object {
        const val APPLICATION_CODE = "app_code"
    }

    lateinit var requestContextMock: RequestContext

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
}