package com.emarsys.mobileengage.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.fake.FakeMobileEngageRefreshTokenInternal
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import java.net.URL

class CoreCompletionHandlerRefreshTokenProxyTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    companion object {
        const val REQUEST_ID = "testRequestId"
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
        const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val INBOX_HOST = "https://me-inbox.eservice.emarsys.net/v3"
    }

    private lateinit var mockCoreCompletionHandler: CoreCompletionHandler
    private lateinit var proxy: CoreCompletionHandlerRefreshTokenProxy
    private lateinit var mockRefreshTokenInternal: RefreshTokenInternal
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockRestClient: RestClient
    private lateinit var mockContactTokenStorage: Storage<String>
    private lateinit var mockPushTokenStorage: Storage<String>
    private lateinit var mockClientServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceProvider: ServiceEndpointProvider
    private lateinit var mockMessageInboxServiceProvider: ServiceEndpointProvider

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockRequestModel = mock(RequestModel::class.java)
        mockResponseModel = mock(ResponseModel::class.java).apply {
            whenever(requestModel).thenReturn(mockRequestModel)
        }

        mockCoreCompletionHandler = mock(CoreCompletionHandler::class.java)
        mockRefreshTokenInternal = mock(RefreshTokenInternal::class.java)
        mockRestClient = mock(RestClient::class.java)
        mockContactTokenStorage = mock(Storage::class.java) as Storage<String>
        mockPushTokenStorage = mock(Storage::class.java) as Storage<String>
        mockClientServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(CLIENT_HOST)
        }
        mockEventServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(EVENT_HOST)
        }
        mockMessageInboxServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(INBOX_HOST)
        }

        proxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandler, mockRefreshTokenInternal, mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)
    }

    @Test
    fun testOnSuccess_shouldDelegate_toCoreCompletionHandler() {
        val mockResponse = mock(ResponseModel::class.java)

        proxy.onSuccess(REQUEST_ID, mockResponse)

        verify(mockCoreCompletionHandler).onSuccess(REQUEST_ID, mockResponse)
    }

    @Test
    fun testOnError_shouldDelegate_toCoreCompletionHandler_whenException() {
        val mockException = mock(Exception::class.java)

        proxy.onError(REQUEST_ID, mockException)

        verify(mockCoreCompletionHandler).onError(REQUEST_ID, mockException)
    }

    @Test
    fun testOnError_shouldCall_createRefreshTokenRequest_whenStatusCodeIs401_andV3EventUrl() {
        whenever(mockRequestModel.url).thenReturn(URL("https://mobile-events.eservice.emarsys.net"))
        whenever(mockResponseModel.statusCode).thenReturn(401)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRefreshTokenInternal).refreshContactToken(any(CompletionListener::class.java))
    }

    @Test
    fun testOnError_shouldCall_clearPushTokenStorage_whenStatusCodeIs401_andV3ClientUrl() {
        whenever(mockRequestModel.url).thenReturn(URL("https://me-client.eservice.emarsys.net"))
        whenever(mockResponseModel.statusCode).thenReturn(401)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockPushTokenStorage).remove()
    }

    @Test
    fun testOnError_shouldCall_createRefreshTokenRequest_whenStatusCodeIs401_andV3InboxUrl() {
        whenever(mockRequestModel.url).thenReturn(URL(INBOX_HOST))
        whenever(mockResponseModel.statusCode).thenReturn(401)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRefreshTokenInternal).refreshContactToken(any(CompletionListener::class.java))
    }

    @Test
    fun testOnError_shouldCall_shouldGiveTheResponseToNextLevel_whenStatusCodeIs401_andNotV3Url() {
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
        proxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandler, FakeMobileEngageRefreshTokenInternal(true), mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)
        val requestModel = RequestModel("https://mobile-events.eservice.emarsys.net", RequestMethod.POST, emptyMap(), mapOf("X-Contact-Token" to "testContactToken", "X-Client-State" to "testClientState"), 12345, Long.MAX_VALUE, REQUEST_ID)

        whenever(mockResponseModel.statusCode).thenReturn(401)
        whenever(mockResponseModel.requestModel).thenReturn(requestModel)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockRestClient).execute(requestModel, proxy)
    }

    @Test
    fun testOnError_shouldCall_coreCompletionHandler_withError_whenExceptionThrown() {
        whenever(mockRequestModel.id).thenReturn(REQUEST_ID)
        whenever(mockRequestModel.url).thenReturn(URL("https://mobile-events.eservice.emarsys.net"))
        whenever(mockResponseModel.statusCode).thenReturn(401)

        proxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandler, FakeMobileEngageRefreshTokenInternal(), mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        proxy.onError(REQUEST_ID, mockResponseModel)

        verify(mockCoreCompletionHandler).onError(eq(REQUEST_ID), any(Exception::class.java))
    }

    @Test
    fun testOnError_shouldCall_coreCompletionHandler_withError_whenExceptionThrown_whenCompositeRequestModel() {
        val mockRequestModel = mock(CompositeRequestModel::class.java).apply {
            whenever(id).thenReturn("compositeRequestId")
            whenever(originalRequestIds).thenReturn(arrayOf(REQUEST_ID))
            whenever(url).thenReturn(URL("https://mobile-events.eservice.emarsys.net"))
        }

        whenever(mockResponseModel.requestModel).thenReturn(mockRequestModel)
        whenever(mockResponseModel.statusCode).thenReturn(401)


        proxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandler, FakeMobileEngageRefreshTokenInternal(), mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        proxy.onError("compositeRequestId", mockResponseModel)

        verify(mockCoreCompletionHandler).onError(eq(REQUEST_ID), any(Exception::class.java))
    }
}