package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
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

class MobileEngageTokenResponseHandlerTest {
    private companion object {
        const val CLIENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val CLIENT_BASE = "$CLIENT_HOST/v3/apps/%s/client"
        const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val INBOX_HOST = "https://mobile-events.eservice.emarsys.net/v3"
    }

    private lateinit var token: String
    private lateinit var tokenKey: String
    private lateinit var tokenResponseHandler: MobileEngageTokenResponseHandler
    private lateinit var mockStorage: Storage<String>
    private lateinit var requestModelMock: RequestModel
    private lateinit var mockClientServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceProvider: ServiceEndpointProvider
    private lateinit var mockMessageInboxServiceProvider: ServiceEndpointProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.bKXKVZCwf8J55WzWagrg2S0o2k_xZQ-HYfHIIj_2Z_U"
        tokenKey = "refreshToken"
        mockStorage = mock(Storage::class.java) as Storage<String>

        mockClientServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(CLIENT_HOST)
        }
        mockEventServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(EVENT_HOST)
        }
        mockMessageInboxServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(INBOX_HOST)
        }

        requestModelMock = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(CLIENT_BASE))
        }
        tokenResponseHandler = MobileEngageTokenResponseHandler(tokenKey, mockStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_whenRequestWasNotForMobileEngage() {
        whenever(requestModelMock.url).thenReturn(URL("https://emarsys.com"))

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
