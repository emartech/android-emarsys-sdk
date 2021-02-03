package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.net.URL

class MobileEngageClientStateResponseHandlerTest {
    private companion object {
        const val X_CLIENT_STATE_VALUE = "TG9yZW0gSXBzdW0gaXMgc2ltcGx5IGR1bW15IHRleHQgb2YgdGhlIHByaW50aW5nIGFuZCB0"
        const val APPLICATION_CODE = "applicationCode"
        const val CLIENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net/v3"
        const val EVENT_HOST_V4 = "https://mobile-events.eservice.emarsys.net/v4"
        const val INBOX_HOST = "https://mobile-events.eservice.emarsys.net/v3"
    }

    private lateinit var mockStorage: StringStorage
    private lateinit var requestModelMock: RequestModel
    private lateinit var clientStateResponseHandler: MobileEngageClientStateResponseHandler
    private lateinit var mockClientServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceV4Provider: ServiceEndpointProvider
    private lateinit var mockMessageInboxServiceProvider: ServiceEndpointProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockStorage = mock(StringStorage::class.java)

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
            whenever(provideEndpointHost()).thenReturn(INBOX_HOST)
        }

        requestModelMock = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(CLIENT_HOST + Endpoint.clientBase(APPLICATION_CODE)))
        }
        clientStateResponseHandler = MobileEngageClientStateResponseHandler(mockStorage, mockClientServiceProvider, mockEventServiceProvider, mockEventServiceV4Provider, mockMessageInboxServiceProvider)
    }

    @Test
    fun testShouldHandleResponse_false_whenUrl_isNotForMobileEngage() {
        whenever(requestModelMock.url).thenReturn(URL("https://not-mobile-engage.com"))

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