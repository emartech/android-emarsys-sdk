package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.URL

class MobileEngageTokenResponseHandlerTest {
    private companion object {
        const val CLIENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val CLIENT_BASE = "$CLIENT_HOST/v3/apps/%s/client"
    }

    private lateinit var token: String
    private lateinit var tokenKey: String
    private lateinit var tokenResponseHandler: MobileEngageTokenResponseHandler
    private lateinit var mockStorage: StringStorage
    private lateinit var requestModelMock: RequestModel
    private lateinit var mockRequestModelHelper: RequestModelHelper


    @BeforeEach
    fun setUp() {
        token =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.bKXKVZCwf8J55WzWagrg2S0o2k_xZQ-HYfHIIj_2Z_U"
        tokenKey = "refreshToken"
        mockStorage = mock()

        requestModelMock = mock {
            on { url } doReturn URL(CLIENT_BASE)
        }
        mockRequestModelHelper = mock {
            on { isMobileEngageRequest(any()) } doReturn true
        }

        tokenResponseHandler = MobileEngageTokenResponseHandler(tokenKey, mockStorage, mockRequestModelHelper)
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_whenRequestWasNotForMobileEngage() {
        whenever(mockRequestModelHelper.isMobileEngageRequest(any())).thenReturn(false)

        val result = tokenResponseHandler.shouldHandleResponse(responseModelWithToken())

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_whenResponseLacksTokenKey() {
        val result = tokenResponseHandler.shouldHandleResponse(responseModelWithoutToken())

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrue_whenResponseBodyIncludes_tokens() {
        val result = tokenResponseHandler.shouldHandleResponse(responseModelWithToken())

        result shouldBe true
    }

    @Test
    fun testHandleResponse_shouldStoreBothToken_whenBothReceived() {
        tokenResponseHandler.handleResponse(responseModelWithToken())

        verify(mockStorage).set(token)
    }

    private fun responseModelWithToken(): ResponseModel {
        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(String.format("{ 'refreshToken': '%s', 'key23': 'value43'}", token))
                .requestModel(requestModelMock)
                .build()
    }

    private fun responseModelWithoutToken(): ResponseModel {
        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body("{ 'key23': 'value43'}")
                .requestModel(requestModelMock)
                .build()
    }
}
