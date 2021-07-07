package com.emarsys.mobileengage.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.fake.FakeMobileEngageRefreshTokenInternal
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.*
import java.net.URL

class CoreCompletionHandlerRefreshTokenProxyTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    companion object {
        const val REQUEST_ID = "testRequestId"
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net/v3"
    }

    private lateinit var mockCoreCompletionHandler: CoreCompletionHandler
    private lateinit var proxy: CoreCompletionHandlerRefreshTokenProxy
    private lateinit var mockRefreshTokenInternal: RefreshTokenInternal
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockRestClient: RestClient
    private lateinit var mockContactTokenStorage: Storage<String?>
    private lateinit var mockPushTokenStorage: Storage<String?>
    private lateinit var mockRequestModelHelper: RequestModelHelper

    @Before
    fun setUp() {
        mockRequestModel = mock()
        mockResponseModel = mock {
            on { requestModel } doReturn mockRequestModel
        }

        mockCoreCompletionHandler = mock()
        mockRefreshTokenInternal = mock()
        mockRestClient = mock()
        mockContactTokenStorage = mock()
        mockPushTokenStorage = mock()

        mockRequestModelHelper = mock {
            on {isMobileEngageRequest(any())} doReturn true
        }

        proxy = CoreCompletionHandlerRefreshTokenProxy(
                mockCoreCompletionHandler,
                mockRefreshTokenInternal,
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage,
                mockRequestModelHelper
        )
    }

    @Test
    fun testOnSuccess_shouldDelegate_toCoreCompletionHandler() {
        val mockResponse: ResponseModel = mock()

        proxy.onSuccess(REQUEST_ID, mockResponse)

        verify(mockCoreCompletionHandler).onSuccess(REQUEST_ID, mockResponse)
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

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRefreshTokenInternal).refreshContactToken(any())
    }

    @Test
    fun testOnError_shouldCall_shouldGiveTheResponseToNextLevel_whenStatusCodeIs401_andNotMobileEngageRequest() {
        whenever(mockResponseModel.statusCode).thenReturn(401)
        whenever(mockRequestModelHelper.isMobileEngageRequest(any())).thenReturn(false)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verifyZeroInteractions(mockRefreshTokenInternal)
        verify(mockCoreCompletionHandler).onError(REQUEST_ID, mockResponseModel)
    }

    @Test
    fun testOnError_shouldGiveTheResponseToNextLevel_whenStatusCodeIsNot401() {
        whenever(mockResponseModel.statusCode).thenReturn(400)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verifyZeroInteractions(mockRefreshTokenInternal)
        verify(mockCoreCompletionHandler).onError(REQUEST_ID, mockResponseModel)

    }

    @Test
    fun testOnError_shouldDelegateRequestModelsContactToken_whenStatusCodeIs401() {
        proxy = CoreCompletionHandlerRefreshTokenProxy(
                mockCoreCompletionHandler,
                FakeMobileEngageRefreshTokenInternal(true),
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage,
                mockRequestModelHelper
        )
        val requestModel = RequestModel(CLIENT_HOST,
                RequestMethod.POST,
                emptyMap(),
                mapOf(
                        "X-Contact-Token" to "testContactToken",
                        "X-Client-State" to "testClientState"),
                12345,
                Long.MAX_VALUE, REQUEST_ID)

        whenever(mockResponseModel.statusCode).thenReturn(401)
        whenever(mockResponseModel.requestModel).thenReturn(requestModel)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRestClient).execute(requestModel, proxy)
    }

    @Test
    fun testOnError_shouldCall_coreCompletionHandler_withError_whenExceptionThrown() {
        whenever(mockRequestModel.id).thenReturn(REQUEST_ID)
        whenever(mockRequestModel.url).thenReturn(URL(CLIENT_HOST))
        whenever(mockResponseModel.statusCode).thenReturn(401)

        proxy = CoreCompletionHandlerRefreshTokenProxy(
                mockCoreCompletionHandler,
                FakeMobileEngageRefreshTokenInternal(),
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage,
                mockRequestModelHelper
        )

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockCoreCompletionHandler).onError(eq(REQUEST_ID), any<Exception>())
    }

    @Test
    fun testOnError_shouldCall_coreCompletionHandler_withError_whenExceptionThrown_whenCompositeRequestModel() {
        val mockRequestModel: CompositeRequestModel = mock {
            on { id } doReturn "compositeRequestId"
            on { originalRequestIds } doReturn arrayOf(REQUEST_ID)
            on { url } doReturn URL(CLIENT_HOST)
        }

        whenever(mockResponseModel.requestModel).thenReturn(mockRequestModel)
        whenever(mockResponseModel.statusCode).thenReturn(401)


        proxy = CoreCompletionHandlerRefreshTokenProxy(
                mockCoreCompletionHandler,
                FakeMobileEngageRefreshTokenInternal(),
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage,
                mockRequestModelHelper
        )

        proxy.onError("compositeRequestId", mockResponseModel)

        verify(mockCoreCompletionHandler).onError(eq(REQUEST_ID), any<Exception>())
    }
}