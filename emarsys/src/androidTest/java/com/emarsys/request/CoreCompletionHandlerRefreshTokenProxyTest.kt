package com.emarsys.request


import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.predict.request.PredictMultiIdRequestModelFactory
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.net.URL

class CoreCompletionHandlerRefreshTokenProxyTest : AnnotationSpec() {


    companion object {
        const val REQUEST_ID = "testRequestId"
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net/v3"
        const val REFRESH_TOKEN = "refreshToken"
    }

    private lateinit var mockCoreCompletionHandler: CoreCompletionHandler
    private lateinit var mockTokenResponseHandler: MobileEngageTokenResponseHandler
    private lateinit var proxy: CoreCompletionHandlerRefreshTokenProxy
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockRestClient: RestClient
    private lateinit var mockContactTokenStorage: Storage<String?>
    private lateinit var mockPushTokenStorage: Storage<String?>
    private lateinit var mockRefreshTokenStorage: Storage<String?>
    private lateinit var mockRequestModelHelper: RequestModelHelper
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var mockPredictMultiIdRequestModelFactory: PredictMultiIdRequestModelFactory

    @Before
    fun setUp() {
        mockRequestModel = mock {
            on { url } doReturn URL(CLIENT_HOST)
        }

        mockResponseModel = mock {
            on { requestModel } doReturn mockRequestModel
        }

        mockCoreCompletionHandler = mock()
        mockRestClient = mock()
        mockContactTokenStorage = mock()
        mockPushTokenStorage = mock()
        mockRefreshTokenStorage = mock()
        mockRequestModelFactory = mock()
        mockPredictMultiIdRequestModelFactory = mock()
        mockTokenResponseHandler = mock()

        mockRequestModelHelper = mock {
            on { isMobileEngageRequest(any()) } doReturn false
        }

        proxy = CoreCompletionHandlerRefreshTokenProxy(
            mockCoreCompletionHandler,
            mockRestClient,
            mockContactTokenStorage,
            mockRefreshTokenStorage,
            mockPushTokenStorage,
            mockTokenResponseHandler,
            mockRequestModelHelper,
            mockRequestModelFactory,
            mockPredictMultiIdRequestModelFactory
        )
    }

    @Test
    fun testOnSuccess_shouldDelegate_toCoreCompletionHandler() {
        proxy.onSuccess(REQUEST_ID, mockResponseModel)

        verify(mockCoreCompletionHandler).onSuccess(REQUEST_ID, mockResponseModel)
    }

    @Test
    fun testOnSuccess_shouldExecuteOriginalRequest_whenMobileEngageRefreshContactTokenRequest_isSuccessful() {
        val testUnauthorizedResponseModel = executeUnauthorizedMobileEngageRequest()

        val testResponseModel = ResponseModel(
            200,
            "testMessage",
            emptyMap(),
            emptyMap(),
            "testBody",
            12345,
            mockRequestModel
        )
        whenever(mockRequestModelHelper.isMobileEngageRefreshContactTokenRequest(mockRequestModel)).thenReturn(
            true
        )

        proxy.onSuccess(REQUEST_ID, testResponseModel)

        verify(mockRestClient).execute(testUnauthorizedResponseModel.requestModel, proxy)
    }

    @Test
    fun testOnSuccess_shouldExecuteOriginalRequest_whenPredictMultiIdRefreshContactTokenRequest_isSuccessful() {
        val testUnauthorizedResponseModel = executeUnauthorizedPredictMultiIdSetContactTokenRequest()

        val testResponseModel = ResponseModel(
            200,
            "testMessage",
            emptyMap(),
            emptyMap(),
            "testBody",
            12345,
            mockRequestModel
        )
        whenever(mockRequestModelHelper.isPredictMultiIdRefreshContactTokenRequest(mockRequestModel)).thenReturn(
            true
        )

        proxy.onSuccess(REQUEST_ID, testResponseModel)

        verify(mockTokenResponseHandler).processResponse(testResponseModel)
        verify(mockRestClient).execute(testUnauthorizedResponseModel.requestModel, proxy)
    }

    @Test
    fun testOnError_shouldDelegate_toCoreCompletionHandler_whenException() {
        val mockException: Exception = mock()

        proxy.onError(REQUEST_ID, mockException)

        verify(mockCoreCompletionHandler).onError(REQUEST_ID, mockException)
    }

    @Test
    fun testOnError_shouldCall_createRefreshTokenRequest_whenStatusCodeIs401_andMobileEngageRequest() {
        whenever(mockRequestModel.url).thenReturn(URL(CLIENT_HOST))
        whenever(mockResponseModel.statusCode).thenReturn(401)
        whenever(mockRequestModelHelper.isMobileEngageRequest(mockRequestModel)).thenReturn(true)
        whenever(mockRequestModelFactory.createRefreshContactTokenRequest()).thenReturn(
            mockRequestModel
        )
        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRestClient).execute(mockRequestModel, proxy)
    }

    @Test
    fun testOnError_shouldCall_completionHandler_withStatusCode418_whenAfterUnauthorizedRequest_MobileEngageRefreshContactTokenRequestFails() {
        val testUnauthorizedResponseModel = executeUnauthorizedMobileEngageRequest()

        val testResponseModel = ResponseModel(
            400,
            "testMessage",
            emptyMap(),
            emptyMap(),
            "testBody",
            12345,
            mockRequestModel
        )
        whenever(mockRequestModelHelper.isMobileEngageRefreshContactTokenRequest(mockRequestModel)).thenReturn(
            true
        )

        proxy.onError(REQUEST_ID, testResponseModel)

        verify(mockCoreCompletionHandler).onError(
            eq(REQUEST_ID),
            eq(testUnauthorizedResponseModel.copy(statusCode = 418))
        )
    }

    @Test
    fun testOnError_shouldCall_completionHandler_withStatusCode418_whenAfterUnauthorizedRequest_PredictMultiIdRefreshContactTokenRequestFails() {
        val testUnauthorizedResponseModel = executeUnauthorizedPredictMultiIdSetContactTokenRequest()

        val testResponseModel = ResponseModel(
            400,
            "testMessage",
            emptyMap(),
            emptyMap(),
            "testBody",
            12345,
            mockRequestModel
        )
        whenever(mockRequestModelHelper.isPredictMultiIdRefreshContactTokenRequest(mockRequestModel)).thenReturn(
            true
        )

        proxy.onError(REQUEST_ID, testResponseModel)

        verify(mockCoreCompletionHandler).onError(
            eq(REQUEST_ID),
            eq(testUnauthorizedResponseModel.copy(statusCode = 418))
        )
    }

    @Test
    fun testOnError_createRefreshTokenRequest_whenStatusCodeIs401_andPredictMultiIdSetContactRequest_andRefreshTokenIsAvailable() {
        whenever(mockRefreshTokenStorage.get()).thenReturn(REFRESH_TOKEN)
        whenever(mockRequestModelHelper.isPredictMultiIdSetContactRequest(mockRequestModel)).thenReturn(
            true
        )
        whenever(mockRequestModel.url).thenReturn(URL(CLIENT_HOST))
        whenever(mockResponseModel.statusCode).thenReturn(401)
        whenever(
            mockPredictMultiIdRequestModelFactory.createRefreshContactTokenRequestModel(
                REFRESH_TOKEN
            )
        ).thenReturn(
            mockRequestModel
        )

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRestClient).execute(mockRequestModel, proxy)
    }

    @Test
    fun testOnError_shouldCall_coreCompletionHandler_whenStatusCodeIs401_andPredictMultiIdSetContactRequest_andRefreshTokenIsNotAvailable() {
        whenever(mockRefreshTokenStorage.get()).thenReturn(null)
        whenever(mockRequestModelHelper.isPredictMultiIdSetContactRequest(mockRequestModel)).thenReturn(
            true
        )
        whenever(mockRequestModel.url).thenReturn(URL(CLIENT_HOST))
        whenever(mockResponseModel.statusCode).thenReturn(401)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockCoreCompletionHandler).onError(eq(REQUEST_ID), eq(mockResponseModel))
    }

    @Test
    fun testOnError_shouldCall_shouldGiveTheResponseToNextLevel_whenStatusCodeIs401_andNotMobileEngageRequest() {
        whenever(mockResponseModel.statusCode).thenReturn(401)
        whenever(mockRequestModelHelper.isMobileEngageRequest(any())).thenReturn(false)
        whenever(mockRequestModelFactory.createRefreshContactTokenRequest()).thenReturn(
            mockRequestModel
        )
        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRestClient, times(0)).execute(mockRequestModel, proxy)
        verify(mockCoreCompletionHandler).onError(REQUEST_ID, mockResponseModel)
    }

    @Test
    fun testOnError_shouldGiveTheResponseToNextLevel_whenStatusCodeIsNot401() {
        whenever(mockResponseModel.statusCode).thenReturn(400)
        whenever(mockRequestModelFactory.createRefreshContactTokenRequest()).thenReturn(
            mockRequestModel
        )

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRestClient, times(0)).execute(mockRequestModel, proxy)
        verify(mockCoreCompletionHandler).onError(REQUEST_ID, mockResponseModel)

    }

    @Test
    fun testOnError_shouldDelegateRequestModelsContactToken_whenStatusCodeIs401() {
        proxy = CoreCompletionHandlerRefreshTokenProxy(
            mockCoreCompletionHandler,
            mockRestClient,
            mockContactTokenStorage,
            mockRefreshTokenStorage,
            mockPushTokenStorage,
            mockTokenResponseHandler,
            mockRequestModelHelper,
            mockRequestModelFactory,
            mockPredictMultiIdRequestModelFactory
        )
        val requestModel = RequestModel(
            CLIENT_HOST,
            RequestMethod.POST,
            emptyMap(),
            mapOf(
                "X-Contact-Token" to "testContactToken",
                "X-Client-State" to "testClientState"
            ),
            12345,
            Long.MAX_VALUE, REQUEST_ID
        )

        whenever(mockResponseModel.statusCode).thenReturn(401)
        whenever(mockRequestModelHelper.isMobileEngageRequest(mockRequestModel)).thenReturn(true)
        whenever(mockRequestModelFactory.createRefreshContactTokenRequest()).thenReturn(requestModel)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRestClient).execute(requestModel, proxy)
    }

    @Test
    fun testOnError_shouldCall_coreCompletionHandler_withError_whenExceptionThrown() {
        whenever(mockRequestModel.id).thenReturn(REQUEST_ID)
        whenever(mockRequestModel.url).thenReturn(URL(CLIENT_HOST))
        whenever(mockResponseModel.statusCode).thenReturn(401)
        whenever(mockRequestModelHelper.isMobileEngageRequest(mockRequestModel)).thenReturn(true)

        val refreshTokenRequestModel: RequestModel = mock()
        whenever(mockRequestModelFactory.createRefreshContactTokenRequest()).thenReturn(
            refreshTokenRequestModel
        )

        whenever(mockRestClient.execute(refreshTokenRequestModel, proxy)).thenAnswer {
            proxy.onError(
                REQUEST_ID, java.lang.Exception()
            )
        }
        proxy = CoreCompletionHandlerRefreshTokenProxy(
            mockCoreCompletionHandler,
            mockRestClient,
            mockContactTokenStorage,
            mockRefreshTokenStorage,
            mockPushTokenStorage,
            mockTokenResponseHandler,
            mockRequestModelHelper,
            mockRequestModelFactory,
            mockPredictMultiIdRequestModelFactory
        )

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockCoreCompletionHandler).onError(eq(REQUEST_ID), any<Exception>())
    }

    private fun executeUnauthorizedMobileEngageRequest(): ResponseModel {
        val mockUnauthorizedRequestModel = mock<RequestModel>()
        val testUnauthorizedResponseModel = ResponseModel(
            401,
            "testUnauthorizedMessage",
            emptyMap(),
            emptyMap(),
            "testBody",
            12345,
            mockUnauthorizedRequestModel
        )
        whenever(mockRequestModelHelper.isMobileEngageRequest(testUnauthorizedResponseModel.requestModel)).thenReturn(
            true
        )
        whenever(
            mockRequestModelFactory.createRefreshContactTokenRequest()
        ).thenReturn(
            mockRequestModel
        )
        proxy.onError(REQUEST_ID, testUnauthorizedResponseModel)
        return testUnauthorizedResponseModel
    }

    private fun executeUnauthorizedPredictMultiIdSetContactTokenRequest(): ResponseModel {
        val mockUnauthorizedRequestModel = mock<RequestModel>()
        val testUnauthorizedResponseModel = ResponseModel(
            401,
            "testUnauthorizedMessage",
            emptyMap(),
            emptyMap(),
            "testBody",
            12345,
            mockUnauthorizedRequestModel
        )
        whenever(mockRefreshTokenStorage.get()).thenReturn(REFRESH_TOKEN)
        whenever(
            mockRequestModelHelper.isPredictMultiIdSetContactRequest(
                testUnauthorizedResponseModel.requestModel
            )
        ).thenReturn(
            true
        )
        whenever(
            mockPredictMultiIdRequestModelFactory.createRefreshContactTokenRequestModel(
                REFRESH_TOKEN
            )
        ).thenReturn(
            mockRequestModel
        )
        proxy.onError(REQUEST_ID, testUnauthorizedResponseModel)
        return testUnauthorizedResponseModel
    }
}
