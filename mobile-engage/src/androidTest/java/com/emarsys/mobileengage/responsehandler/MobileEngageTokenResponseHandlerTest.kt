package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
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

class MobileEngageTokenResponseHandlerTest {

    private lateinit var token: String
    private lateinit var tokenKey: String
    private lateinit var tokenResponseHandler: MobileEngageTokenResponseHandler
    private lateinit var mockStorage: Storage<String>
    private lateinit var requestModelMock: RequestModel

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.bKXKVZCwf8J55WzWagrg2S0o2k_xZQ-HYfHIIj_2Z_U"
        tokenKey = "refreshToken"
        mockStorage = mock(Storage::class.java) as Storage<String>

        requestModelMock = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_CLIENT_BASE))
        }
        tokenResponseHandler = MobileEngageTokenResponseHandler(tokenKey, mockStorage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_tokenKey_mustNotBeNull() {
        MobileEngageTokenResponseHandler(null, mockStorage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_tokenStorage_mustNotBeNull() {
        MobileEngageTokenResponseHandler(tokenKey, null)
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
