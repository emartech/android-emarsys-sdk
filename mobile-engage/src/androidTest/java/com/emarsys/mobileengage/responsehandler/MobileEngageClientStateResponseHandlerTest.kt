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
import org.mockito.Mockito
import org.mockito.Mockito.verify
import java.net.URL

class MobileEngageClientStateResponseHandlerTest {
    private companion object {
        const val X_CLIENT_STATE_VALUE = "TG9yZW0gSXBzdW0gaXMgc2ltcGx5IGR1bW15IHRleHQgb2YgdGhlIHByaW50aW5nIGFuZCB0"
    }

    private lateinit var mockStorage: Storage<String>
    private lateinit var requestModelMock: RequestModel
    private lateinit var clientStateResponseHandler: MobileEngageClientStateResponseHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockStorage = Mockito.mock(Storage::class.java) as Storage<String>

        requestModelMock = Mockito.mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_CLIENT_BASE))
        }
        clientStateResponseHandler = MobileEngageClientStateResponseHandler(mockStorage)
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