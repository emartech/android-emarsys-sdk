package com.emarsys.mobileengage.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.worker.CoreCompletionHandlerMiddleware
import com.emarsys.core.worker.Worker
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock

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
    private lateinit var mockEventServiceV4Provider: ServiceEndpointProvider
    private lateinit var mockMessageInboxServiceProvider: ServiceEndpointProvider
    private lateinit var mockCoreCompletionHandler: CoreCompletionHandler
    private lateinit var mockDefaultCoreCompletionHandler: CoreCompletionHandler
    private lateinit var mockRequestModelHelper: RequestModelHelper

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockCoreCompletionHandlerMiddlewareProvider = mock()
        mockCoreCompletionHandlerMiddleware = mock()
        mockRefreshTokenInternal = mock()
        mockRestClient = mock()
        mockContactTokenStorage = mock()
        mockPushTokenStorage = mock()
        mockClientServiceProvider = mock()
        mockEventServiceProvider = mock()
        mockEventServiceV4Provider = mock()
        mockMessageInboxServiceProvider = mock()
        mockCoreCompletionHandler = mock()
        mockDefaultCoreCompletionHandler = mock()
        mockRequestModelHelper = mock()

        coreCompletionHandlerRefreshTokenProxyProvider = CoreCompletionHandlerRefreshTokenProxyProvider(
                mockCoreCompletionHandlerMiddlewareProvider,
                mockRefreshTokenInternal,
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage,
                mockDefaultCoreCompletionHandler,
                mockRequestModelHelper
        )
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy() {
        val mockWorker = mock(Worker::class.java)
        whenever(mockCoreCompletionHandlerMiddlewareProvider.provideProxy(mockWorker, mockCoreCompletionHandler)).thenReturn(mockCoreCompletionHandlerMiddleware)
        val expectedProxy = CoreCompletionHandlerRefreshTokenProxy(
                mockCoreCompletionHandlerMiddleware, mockRefreshTokenInternal, mockRestClient,
                mockContactTokenStorage, mockPushTokenStorage, mockRequestModelHelper)

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(mockWorker, mockCoreCompletionHandler)

        result shouldBe expectedProxy
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy_whenNoHandlerIsAvailable() {
        whenever(mockCoreCompletionHandlerMiddlewareProvider.provideProxy(null, null)).thenReturn(mockDefaultCoreCompletionHandler)
        val expectedProxy = CoreCompletionHandlerRefreshTokenProxy(
                mockDefaultCoreCompletionHandler, mockRefreshTokenInternal, mockRestClient,
                mockContactTokenStorage, mockPushTokenStorage, mockRequestModelHelper)

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(null, null)

        result shouldBe expectedProxy
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy_whenNoWorkerIsAvailable() {

        whenever(mockCoreCompletionHandlerMiddlewareProvider.provideProxy(null, mockCoreCompletionHandler)).thenReturn(mockCoreCompletionHandler)
        val expectedProxy = CoreCompletionHandlerRefreshTokenProxy(
                mockCoreCompletionHandler, mockRefreshTokenInternal, mockRestClient,
                mockContactTokenStorage, mockPushTokenStorage, mockRequestModelHelper)

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(null, mockCoreCompletionHandler)

        result shouldBe expectedProxy
    }
}