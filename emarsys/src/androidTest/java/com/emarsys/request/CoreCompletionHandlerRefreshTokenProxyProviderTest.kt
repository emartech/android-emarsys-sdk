package com.emarsys.request


import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.worker.CoreCompletionHandlerMiddleware
import com.emarsys.core.worker.Worker
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock

class CoreCompletionHandlerRefreshTokenProxyProviderTest : AnnotationSpec() {
    private lateinit var mockCoreCompletionHandlerMiddlewareProvider: CoreCompletionHandlerMiddlewareProvider
    private lateinit var mockCoreCompletionHandlerMiddleware: CoreCompletionHandlerMiddleware
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
    private lateinit var mockTokenResponseHandler: MobileEngageTokenResponseHandler
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory


    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockCoreCompletionHandlerMiddlewareProvider = mock()
        mockCoreCompletionHandlerMiddleware = mock()
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
        mockTokenResponseHandler = mock()
        mockRequestModelFactory = mock()

        coreCompletionHandlerRefreshTokenProxyProvider =
            com.emarsys.request.CoreCompletionHandlerRefreshTokenProxyProvider(
                mockCoreCompletionHandlerMiddlewareProvider,
                mockRestClient,
                mockContactTokenStorage,
                mockPushTokenStorage,
                mockDefaultCoreCompletionHandler,
                mockRequestModelHelper,
                mockTokenResponseHandler,
                mockRequestModelFactory
            )
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy() {
        val mockWorker = mock(Worker::class.java)
        whenever(
            mockCoreCompletionHandlerMiddlewareProvider.provideProxy(
                mockWorker,
                mockCoreCompletionHandler
            )
        ).thenReturn(mockCoreCompletionHandlerMiddleware)
        val expectedProxy = com.emarsys.request.CoreCompletionHandlerRefreshTokenProxy(
            mockCoreCompletionHandlerMiddleware,
            mockRestClient,
            mockContactTokenStorage,
            mockPushTokenStorage,
            mockTokenResponseHandler,
            mockRequestModelHelper,
            mockRequestModelFactory
        )

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(
            mockWorker,
            mockCoreCompletionHandler
        )

        result shouldBe expectedProxy
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy_whenNoHandlerIsAvailable() {
        whenever(mockCoreCompletionHandlerMiddlewareProvider.provideProxy(null, null)).thenReturn(
            mockDefaultCoreCompletionHandler
        )

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(null, null)

        result.javaClass shouldBe com.emarsys.request.CoreCompletionHandlerRefreshTokenProxy::class.java
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy_whenNoWorkerIsAvailable() {

        whenever(
            mockCoreCompletionHandlerMiddlewareProvider.provideProxy(
                null,
                mockCoreCompletionHandler
            )
        ).thenReturn(mockCoreCompletionHandler)

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(
            null,
            mockCoreCompletionHandler
        )

        result.javaClass shouldBe com.emarsys.request.CoreCompletionHandlerRefreshTokenProxy::class.java
    }
}