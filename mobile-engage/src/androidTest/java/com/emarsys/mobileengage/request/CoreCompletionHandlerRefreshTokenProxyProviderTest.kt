package com.emarsys.mobileengage.request

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.storage.Storage
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
    private lateinit var mockContactTokenStorage: Storage<String>
    private lateinit var mockPushTokenStorage: Storage<String>
    private lateinit var coreCompletionHandlerRefreshTokenProxyProvider: CoreCompletionHandlerRefreshTokenProxyProvider
    private lateinit var mockClientServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceProvider: ServiceEndpointProvider
    private lateinit var mockMessageInboxServiceProvider: ServiceEndpointProvider

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
        mockContactTokenStorage = mock(Storage::class.java) as Storage<String>
        mockPushTokenStorage = mock(Storage::class.java) as Storage<String>
        mockClientServiceProvider = mock(ServiceEndpointProvider::class.java)
        mockEventServiceProvider = mock(ServiceEndpointProvider::class.java)
        mockMessageInboxServiceProvider = mock(ServiceEndpointProvider::class.java)

        coreCompletionHandlerRefreshTokenProxyProvider = CoreCompletionHandlerRefreshTokenProxyProvider(mockCoreCompletionHandlerMiddlewareProvider, mockRefreshTokenInternal, mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_coreCompletionHandlerMiddlewareProvider_mustNotBeNull() {
        CoreCompletionHandlerRefreshTokenProxyProvider(null, mockRefreshTokenInternal, mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_refreshTokenInternal_mustNotBeNull() {
        CoreCompletionHandlerRefreshTokenProxyProvider(mockCoreCompletionHandlerMiddlewareProvider, null, mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_restClient_mustNotBeNull() {
        CoreCompletionHandlerRefreshTokenProxyProvider(mockCoreCompletionHandlerMiddlewareProvider, mockRefreshTokenInternal, null, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_contactTokenStorage_mustNotBeNull() {
        CoreCompletionHandlerRefreshTokenProxyProvider(mockCoreCompletionHandlerMiddlewareProvider, mockRefreshTokenInternal, mockRestClient, null, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_pushTokenStorage_mustNotBeNull() {
        CoreCompletionHandlerRefreshTokenProxyProvider(mockCoreCompletionHandlerMiddlewareProvider, mockRefreshTokenInternal, mockRestClient, mockContactTokenStorage, null, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_clientServiceProvider_mustNotBeNull() {
        CoreCompletionHandlerRefreshTokenProxyProvider(mockCoreCompletionHandlerMiddlewareProvider, mockRefreshTokenInternal, mockRestClient, mockContactTokenStorage, mockPushTokenStorage, null, mockEventServiceProvider, mockMessageInboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_eventServiceProvider_mustNotBeNull() {
        CoreCompletionHandlerRefreshTokenProxyProvider(mockCoreCompletionHandlerMiddlewareProvider, mockRefreshTokenInternal, mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, null, mockMessageInboxServiceProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_messageInboxServiceProvider_mustNotBeNull() {
        CoreCompletionHandlerRefreshTokenProxyProvider(mockCoreCompletionHandlerMiddlewareProvider, mockRefreshTokenInternal, mockRestClient, mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testProvideCoreCompletionHandlerRefreshTokenProxy_worker_mustNotBeNull() {
        coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(null)
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy() {
        val mockWorker = mock(Worker::class.java)
        whenever(mockCoreCompletionHandlerMiddlewareProvider.provideProxy(mockWorker)).thenReturn(mockCoreCompletionHandlerMiddleware)
        val expectedProxy = CoreCompletionHandlerRefreshTokenProxy(mockCoreCompletionHandlerMiddleware, mockRefreshTokenInternal, mockRestClient,
                mockContactTokenStorage, mockPushTokenStorage, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(mockWorker)

        result shouldBe expectedProxy
    }
}