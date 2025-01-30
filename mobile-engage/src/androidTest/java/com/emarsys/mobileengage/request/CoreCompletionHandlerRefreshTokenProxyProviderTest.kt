package com.emarsys.mobileengage.request


import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.worker.CoreCompletionHandlerMiddleware
import com.emarsys.core.worker.Worker
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.util.RequestModelHelper
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class CoreCompletionHandlerRefreshTokenProxyProviderTest  {
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
        mockCoreCompletionHandlerMiddlewareProvider = mockk(relaxed = true)
        mockCoreCompletionHandlerMiddleware = mockk(relaxed = true)
        mockRestClient = mockk(relaxed = true)
        mockContactTokenStorage = mockk(relaxed = true)
        mockPushTokenStorage = mockk(relaxed = true)
        mockClientServiceProvider = mockk(relaxed = true)
        mockEventServiceProvider = mockk(relaxed = true)
        mockEventServiceV4Provider = mockk(relaxed = true)
        mockMessageInboxServiceProvider = mockk(relaxed = true)
        mockCoreCompletionHandler = mockk(relaxed = true)
        mockDefaultCoreCompletionHandler = mockk(relaxed = true)
        mockRequestModelHelper = mockk(relaxed = true)
        mockTokenResponseHandler = mockk(relaxed = true)
        mockRequestModelFactory = mockk(relaxed = true)

        coreCompletionHandlerRefreshTokenProxyProvider =
            CoreCompletionHandlerRefreshTokenProxyProvider(
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
        val mockWorker: Worker = mockk(relaxed = true)
        every {
            mockCoreCompletionHandlerMiddlewareProvider.provideProxy(
                mockWorker,
                mockCoreCompletionHandler
            )
        } returns mockCoreCompletionHandlerMiddleware

        val expectedProxy = CoreCompletionHandlerRefreshTokenProxy(
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
        every { mockCoreCompletionHandlerMiddlewareProvider.provideProxy(null, null) } returns
                mockDefaultCoreCompletionHandler

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(null, null)

        result.javaClass shouldBe CoreCompletionHandlerRefreshTokenProxy::class.java
    }

    @Test
    fun testProvideCoreCompletionHandlerRefreshTokenProxy_whenNoWorkerIsAvailable() {

        every {
            mockCoreCompletionHandlerMiddlewareProvider.provideProxy(
                null,
                mockCoreCompletionHandler
            )
        } returns mockCoreCompletionHandler

        val result = coreCompletionHandlerRefreshTokenProxyProvider.provideProxy(
            null,
            mockCoreCompletionHandler
        )

        result.javaClass shouldBe CoreCompletionHandlerRefreshTokenProxy::class.java
    }
}