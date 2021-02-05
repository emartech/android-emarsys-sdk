package com.emarsys.mobileengage.request

import android.os.Handler
import android.os.Looper
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.fake.FakeMobileEngageRefreshTokenInternal
import com.emarsys.mobileengage.testUtil.DependencyTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
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
    private lateinit var mockContactTokenStorage: Storage<String>
    private lateinit var mockPushTokenStorage: Storage<String>

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

        DependencyTestUtils.setupDependencyInjectionWithServiceProviders()

        proxy = CoreCompletionHandlerRefreshTokenProxy(
                mockCoreCompletionHandler,
                mockRefreshTokenInternal,
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage)
    }

    @After
    fun tearDown() {
        val handler = getDependency<Handler>("coreSdkHandler")
        val looper: Looper? = handler.looper
        looper?.quit()
        DependencyInjection.tearDown()
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

        verify(mockRefreshTokenInternal).refreshContactToken(any(CompletionListener::class.java))
    }

    @Test
    fun testOnError_shouldCall_shouldGiveTheResponseToNextLevel_whenStatusCodeIs401_andNotMobileEngageRequest() {
        whenever(mockRequestModel.url).thenReturn(URL("https://www.emarsys.com"))
        whenever(mockResponseModel.statusCode).thenReturn(401)

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
        proxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandler,
                FakeMobileEngageRefreshTokenInternal(true),
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage)
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

        proxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandler,
                FakeMobileEngageRefreshTokenInternal(),
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockCoreCompletionHandler).onError(eq(REQUEST_ID), any(Exception::class.java))
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


        proxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandler,
                FakeMobileEngageRefreshTokenInternal(),
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage)

        proxy.onError("compositeRequestId", mockResponseModel)

        verify(mockCoreCompletionHandler).onError(eq(REQUEST_ID), any(Exception::class.java))
    }
}