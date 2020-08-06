package com.emarsys.mobileengage.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.worker.CoreCompletionHandlerMiddleware
import com.emarsys.core.worker.Worker
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class CoreCompletionHandlerRefreshTokenProxyProviderTest {
    private lateinit var mockCoreCompletionHandlerMiddlewareProvider: CoreCompletionHandlerMiddlewareProvider
    private lateinit var mockCoreCompletionHandlerMiddleware: CoreCompletionHandlerMiddleware
    private lateinit var mockRefreshTokenInternal: RefreshTokenInternal
    private lateinit var mockRestClient: RestClient
    private lateinit var mockContactTokenStorage: StringStorage
    private lateinit var mockPushTokenStorage: StringStorage
    private lateinit var coreCompletionHandlerRefreshTokenProxyProvider: CoreCompletionHandlerRefreshTokenProxyProvider
    private lateinit var mockClientServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceProvider: ServiceEndpointProvider
    private lateinit var mockMessageInboxServiceProvider: ServiceEndpointProvider
    private lateinit var mockCoreCompletionHandler: CoreCompletionHandler
    private lateinit var mockDefaultCoreCompletionHandler: CoreCompletionHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockCoreCompletionHandlerMiddlewareProvider = mock(CoreCompletionHandlerMiddlewareProvider::class.java)
        mockCoreCompletionHandlerMiddleware = mock(CoreCompletionHandlerMiddleware::class.java)
        mockRefreshTokenInternal = mock(RefreshTokenInternal::class.java)
        mockRestClient = mock(RestClient::class.java)
        mockContactTokenStorage = mock(StringStorage::class.java)
        mockPushTokenStorage = mock(StringStorage::class.java)
        mockClientServiceProvider = mock(ServiceEndpointProvider::class.java)
        mockEventServiceProvider = mock(ServiceEndpointProvider::class.java)
        mockMessageInboxServiceProvider = mock(ServiceEndpointProvider::class.java)
        mockCoreCompletionHandler = mock(CoreCompletionHandler::class.java)
        mockDefaultCoreCompletionHandler = mock(CoreCompletionHandler::class.java)

        coreCompletionHandlerRefreshTokenProxyProvider = CoreCompletionHandlerRefreshTokenProxyProvider(mockCoreCompletionHandlerMiddlewareProvider, mockRefreshTokenInternal, mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider, mockDefaultCoreCompletionHandler)
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy() {
        val mockWorker = mock(Worker::class.java)
        whenever(mockCoreCompletionHandlerMiddlewareProvider.provideProxy(mockWorker, mockCoreCompletionHandler)).thenReturn(mockCoreCompletionHandlerMiddleware)
        val expectedProxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandlerMiddleware, mockRefreshTokenInternal, mockRestClient,
                mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(mockWorker, mockCoreCompletionHandler)

        result shouldBe expectedProxy
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy_whenNoHandlerIsAvailable() {
        whenever(mockCoreCompletionHandlerMiddlewareProvider.provideProxy(null, null)).thenReturn(mockDefaultCoreCompletionHandler)
        val expectedProxy = CoreCompletionHandlerRefreshTokenProxy(mockDefaultCoreCompletionHandler, mockRefreshTokenInternal, mockRestClient,
                mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(null,null)

        result shouldBe expectedProxy
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy_whenNoWorkerIsAvailable() {

        whenever(mockCoreCompletionHandlerMiddlewareProvider.provideProxy(null, mockCoreCompletionHandler)).thenReturn(mockCoreCompletionHandler)
        val expectedProxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandler, mockRefreshTokenInternal, mockRestClient,
                mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(null, mockCoreCompletionHandler)

        result shouldBe expectedProxy
    }
}