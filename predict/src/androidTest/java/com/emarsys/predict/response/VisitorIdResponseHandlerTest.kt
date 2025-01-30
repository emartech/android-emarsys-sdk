package com.emarsys.predict.response


import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.testUtil.mockito.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class VisitorIdResponseHandlerTest  {


    private lateinit var keyValueStore: KeyValueStore
    private lateinit var mockServiceEndpointProvider: ServiceEndpointProvider
    private lateinit var responseHandler: VisitorIdResponseHandler

    @Before
    fun init() {
        keyValueStore = mock(KeyValueStore::class.java)
        mockServiceEndpointProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn("https://recommender.scarabresearch.com")
        }
        responseHandler = VisitorIdResponseHandler(keyValueStore, mockServiceEndpointProvider)
    }

    @Test
    fun testConstructor_keyValueStore_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            VisitorIdResponseHandler(null, mockServiceEndpointProvider)
        }
    }

    @Test
    fun testConstructor_mockServiceEndpointProvider_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            VisitorIdResponseHandler(keyValueStore, null)
        }
    }

    @Test
    fun testShouldHandleResponse_withoutPredictResponse_withoutVisitorIdCookie() {
        val requestModel = RequestModel.Builder(TimestampProvider(), UUIDProvider())
                .url("https://emarsys.com")
                .build()
        val response = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .requestModel(requestModel)
                .build()
        responseHandler.shouldHandleResponse(response) shouldBe false
    }

    @Test
    fun testShouldHandleResponse_withPredictResponse_withoutVisitorIdCookie() {
        val requestModel = RequestModel.Builder(TimestampProvider(), UUIDProvider())
            .url("https://recommender.scarabresearch.com/merchants/2345432")
            .build()
        val response = ResponseModel.Builder()
            .statusCode(200)
            .message("OK")
            .requestModel(requestModel)
            .build()

        responseHandler.shouldHandleResponse(response) shouldBe false
    }

    @Test
    fun testShouldHandleResponse_withoutPredictResponse_withVisitorIdCookie() {
        val requestModel = RequestModel.Builder(TimestampProvider(), UUIDProvider())
                .url("https://emarsys.com")
                .build()
        val headers = mapOf(
                null to listOf("HTTP/1.1 200 OK"),
                "Set-Cookie" to listOf(
                        "cdv=AAABBB;Path=/;Expires=Fri, 20-Sep-2019 14:30:24 GMT",
                        "s=ASDF1234"
                )
        )
        val response = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .headers(headers)
                .requestModel(requestModel)
                .build()
        responseHandler.shouldHandleResponse(response) shouldBe false
    }

    @Test
    fun testShouldHandleResponse_withPredictResponse_withVisitorIdCookie() {
        val requestModel = RequestModel.Builder(TimestampProvider(), UUIDProvider())
                .url("https://recommender.scarabresearch.com/merchants/2345432")
                .build()
        val headers = mapOf(
                null to listOf("HTTP/1.1 200 OK"),
                "Set-Cookie" to listOf(
                        "cdv=AAABBB;Path=/;Expires=Fri, 20-Sep-2019 14:30:24 GMT",
                        "s=ASDF1234"
                )
        )
        val response = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .headers(headers)
                .requestModel(requestModel)
                .build()
        responseHandler.shouldHandleResponse(response) shouldBe true
    }

    @Test
    fun testHandleResponse_shouldStoreVisitorId() {
        val visitorId = "AAABBB"
        val requestModel = RequestModel.Builder(TimestampProvider(), UUIDProvider())
                .url("https://recommender.scarabresearch.com/merchants/2345432")
                .build()
        val headers = mapOf(
                null to listOf("HTTP/1.1 200 OK"),
                "Set-Cookie" to listOf(
                        "cdv=$visitorId;Path=/;Expires=Fri, 20-Sep-2019 14:30:24 GMT",
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

        verify(keyValueStore).putString("predict_visitor_id", visitorId)
    }

}