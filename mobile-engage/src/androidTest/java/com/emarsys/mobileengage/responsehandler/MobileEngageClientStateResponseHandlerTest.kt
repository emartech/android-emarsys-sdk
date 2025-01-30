package com.emarsys.mobileengage.responsehandler


import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.URL

class MobileEngageClientStateResponseHandlerTest  {
    private companion object {
        const val X_CLIENT_STATE_VALUE =
            "TG9yZW0gSXBzdW0gaXMgc2ltcGx5IGR1bW15IHRleHQgb2YgdGhlIHByaW50aW5nIGFuZCB0"
        const val APPLICATION_CODE = "applicationCode"
        const val CLIENT_HOST = "https://mobile-events.eservice.emarsys.net/v3"
    }

    private lateinit var mockStorage: StringStorage
    private lateinit var requestModelMock: RequestModel
    private lateinit var clientStateResponseHandler: MobileEngageClientStateResponseHandler
    private lateinit var mockRequestModelHelper: RequestModelHelper


    @Before
    fun setUp() {
        mockStorage = mock()
        requestModelMock = mock {
            on { url } doReturn URL(CLIENT_HOST + Endpoint.clientBase(APPLICATION_CODE))
        }
        mockRequestModelHelper = mock {
            on { isMobileEngageRequest(any()) } doReturn true
        }

        clientStateResponseHandler =
            MobileEngageClientStateResponseHandler(mockStorage, mockRequestModelHelper)
    }

    @Test
    fun testShouldHandleResponse_false_whenUrl_isNotForMobileEngage() {
        whenever(mockRequestModelHelper.isMobileEngageRequest(any())).thenReturn(false)

        val result = clientStateResponseHandler.shouldHandleResponse(responseModelWithClientState())

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_false_whenClientStateIsNotPresent() {
        val result = clientStateResponseHandler.shouldHandleResponse(responseModelWithoutClientState())

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_true_whenRequestWasForMobileEngage_andContainsClientState() {
        val result = clientStateResponseHandler.shouldHandleResponse(responseModelWithClientState())

        result shouldBe true
    }

    @Test
    fun testShouldHandleResponse_true_whenRequestWasForMobileEngage_andContainsClientState_shouldBeCaseInsensitive() {
        val result = clientStateResponseHandler.shouldHandleResponse(responseModelWithClientState(clientState = "X-ClIeNt-stAtE"))

        result shouldBe true
    }

    @Test
    fun testHandleResponse_storesClientState() {
        clientStateResponseHandler.handleResponse(responseModelWithClientState())

        verify(mockStorage).set(X_CLIENT_STATE_VALUE)
    }

    @Test
    fun testHandleResponse_storesClientState_shouldBeCaseInsensitive() {
        clientStateResponseHandler.handleResponse(responseModelWithClientState(clientState = "X-ClIeNt-stAtE"))

        verify(mockStorage).set(X_CLIENT_STATE_VALUE)
    }

    private fun responseModelWithClientState(clientState: String = "X-Client-State"): ResponseModel {
        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .headers(mapOf(clientState to listOf(X_CLIENT_STATE_VALUE)))
                .requestModel(requestModelMock)
                .build()
    }

    private fun responseModelWithoutClientState(): ResponseModel {
        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .requestModel(requestModelMock)
                .build()
    }
}