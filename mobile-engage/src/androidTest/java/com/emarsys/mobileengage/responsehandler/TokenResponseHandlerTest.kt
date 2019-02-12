package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.storage.ContactTokenStorage
import com.emarsys.mobileengage.storage.RefreshTokenStorage
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class TokenResponseHandlerTest {

    private lateinit var refreshToken: String
    private lateinit var contactToken: String
    private lateinit var handler: TokenResponseHandler
    private lateinit var refreshTokenStorage: RefreshTokenStorage
    private lateinit var contactTokenStorage: ContactTokenStorage
    private lateinit var responseModelWithTokens: ResponseModel

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ4IjoieSJ9.bKXKVZCwf8J55WzWagrg2S0o2k_xZQ-HYfHIIj_2Z_U"
        contactToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhIjoiYiJ9.dFuAWy0_-WromuzYCXT4g9hJRs1NI90lc7HXac1Q-ZA"
        refreshTokenStorage = mock(RefreshTokenStorage::class.java)
        contactTokenStorage = mock(ContactTokenStorage::class.java)
        handler = TokenResponseHandler(refreshTokenStorage, contactTokenStorage)

        responseModelWithTokens = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(String.format("{ 'refreshToken': '%s', 'contactToken': '%s'}", refreshToken, contactToken))
                .requestModel(mock(RequestModel::class.java))
                .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_meIdStorage_mustNotBeNull() {
        TokenResponseHandler(null, contactTokenStorage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_meIdSignatureStorage_mustNotBeNull() {
        TokenResponseHandler(refreshTokenStorage, null)
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrue_whenResponseBodyIncludes_tokens() {
        val result = handler.shouldHandleResponse(responseModelWithTokens)

        result shouldBe true
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrue_whenResponseBodyLacks_contactToken() {
        val responseModel = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body("{ 'contactToken': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9' }")
                .requestModel(mock(RequestModel::class.java))
                .build()

        val result = handler.shouldHandleResponse(responseModel)

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrue_whenResponseBodyLacks_refreshToken() {
        val responseModel = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body("{ 'refreshToken': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9' }")
                .requestModel(mock(RequestModel::class.java))
                .build()

        val result = handler.shouldHandleResponse(responseModel)

        result shouldBe false
    }

    @Test
    fun testHandleResponse_shouldStoreRefreshToken() {
        handler.handleResponse(responseModelWithTokens)

        verify(refreshTokenStorage).set(refreshToken)
    }

    @Test
    fun testHandleResponse_shouldStoreContactToken() {
        handler.handleResponse(responseModelWithTokens)

        verify(contactTokenStorage).set(contactToken)
    }
}