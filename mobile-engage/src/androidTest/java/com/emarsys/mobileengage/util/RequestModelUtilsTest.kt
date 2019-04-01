package com.emarsys.mobileengage.util

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.net.URL

class RequestModelUtilsTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test(expected = IllegalArgumentException::class)
    fun testIsMobileEngageRequest_requestModel_mustNotBeNull() {
        RequestModelUtils.isMobileEngageRequest(null)
    }

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageClient() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_CLIENT_BASE))
        }
        val result = RequestModelUtils.isMobileEngageRequest(mockRequestModel)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageEvent() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_EVENT_BASE))
        }
        val result = RequestModelUtils.isMobileEngageRequest(mockRequestModel)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_false_whenItIsNotMobileEngage() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL("https://not-mobile-engage.com"))
        }
        val result = RequestModelUtils.isMobileEngageRequest(mockRequestModel)

        result shouldBe false
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsCustomEvent_V3_requestModel_mustNotBeNull() {
        RequestModelUtils.isCustomEvent_V3(null)
    }

    @Test
    fun testIsCustomEvent_V3_true_whenItIsCustomEventV3Event() {
        val requestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_EVENT_BASE))
        }
        val result = RequestModelUtils.isCustomEvent_V3(requestModel)

        result shouldBe true
    }

    @Test
    fun testIsCustomEvent_V3_false_whenItIsNotCustomEventV3Event() {
        val requestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_CLIENT_BASE))
        }
        val result = RequestModelUtils.isCustomEvent_V3(requestModel)

        result shouldBe false
    }
}