package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
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

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockStorage = Mockito.mock(Storage::class.java) as Storage<String>

        requestModelMock = Mockito.mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_CLIENT_BASE))
        }
        clientStateResponseHandler = MobileEngageClientStateResponseHandler(mockStorage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_clientStateStorage_mustNotBeNull() {
        MobileEngageClientStateResponseHandler(null)
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
    fun testHandleResponse_storesClientState() {
        clientStateResponseHandler.handleResponse(responseModelWithClientState())

        verify(mockStorage).set(X_CLIENT_STATE_VALUE)
    }

    private fun responseModelWithClientState(): ResponseModel {
        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .headers(mapOf("X-CLIENT-STATE" to listOf(X_CLIENT_STATE_VALUE)))
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