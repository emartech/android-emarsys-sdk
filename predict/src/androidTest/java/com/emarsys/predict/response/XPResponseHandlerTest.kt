package com.emarsys.predict.response

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import org.mockito.Mockito.mock

class XPResponseHandlerTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var keyValueStore: KeyValueStore
    private lateinit var responseHandler: XPResponseHandler
    private lateinit var mockPredictServiceProvider: ServiceEndpointProvider

    @Before
    fun init() {
        mockPredictServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn("https://recommender.scarabresearch.com")
        }
        keyValueStore = mock(KeyValueStore::class.java)
        responseHandler = XPResponseHandler(keyValueStore, mockPredictServiceProvider)
    }

    @Test
    fun testShouldHandlerResponse_withoutPredictResponse_withoutXPCookie() {
        val requestModel = RequestModel.Builder(TimestampProvider(), UUIDProvider())
                .url("https://emarsys.com")
                .build()
        val response = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .requestModel(requestModel)
                .build()
        assertFalse(responseHandler.shouldHandleResponse(response))
    }

    @Test
    fun testShouldHandleResponse_withPredictResponse_withXPCookie() {
        val requestModel = RequestModel.Builder(TimestampProvider(), UUIDProvider())
                .url("https://recommender.scarabresearch.com/merchants/2345432")
                .build()
        val headers = mapOf(
                null to listOf("HTTP/1.1 200 OK"),
                "Set-Cookie" to listOf(
                        "xp=AAABBB;Path=/;Expires=Fri, 20-Sep-2019 14:30:24 GMT",
                        "s=ASDF1234"
                )
        )
        val response = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .headers(headers)
                .requestModel(requestModel)
                .build()
        assertTrue(responseHandler.shouldHandleResponse(response))
    }


    @Test
    fun testShouldNotHandleResponse_withoutPredictResponse_withXPCookie() {
        val requestModel = RequestModel.Builder(TimestampProvider(), UUIDProvider())
                .url("https://www.emarsys.com")
                .build()
        val headers = mapOf(
                null to listOf("HTTP/1.1 200 OK"),
                "Set-Cookie" to listOf(
                        "xp=AAABBB;Path=/;Expires=Fri, 20-Sep-2019 14:30:24 GMT",
                        "s=ASDF1234"
                )
        )
        val response = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .headers(headers)
                .requestModel(requestModel)
                .build()
        assertFalse(responseHandler.shouldHandleResponse(response))
    }

    @Test
    fun testHandleResponse_shouldStoreXP() {
        val xp = "AAABBB"
        val requestModel = RequestModel.Builder(TimestampProvider(), UUIDProvider())
                .url("https://recommender.scarabresearch.com/merchants/2345432")
                .build()
        val headers = mapOf(
                null to listOf("HTTP/1.1 200 OK"),
                "Set-Cookie" to listOf(
                        "xp=$xp;Path=/;Expires=Fri, 20-Sep-2019 14:30:24 GMT",
                        "s=ASDF1234"
                )
        )
        val response = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .headers(headers)
                .requestModel(requestModel)
                .build()

        responseHandler.handleResponse(response)

        Mockito.verify(keyValueStore).putString("xp", xp)
    }

}