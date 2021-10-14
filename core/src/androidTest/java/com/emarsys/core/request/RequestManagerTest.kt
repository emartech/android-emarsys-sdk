package com.emarsys.core.request

import android.os.Handler
import android.os.Looper
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mapper
import com.emarsys.core.Registry
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.fake.FakeCompletionHandler
import com.emarsys.core.fake.FakeRunnableFactory
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.request.factory.ScopeDelegatorCompletionHandlerProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.worker.DefaultWorker
import com.emarsys.core.worker.Worker
import com.emarsys.testUtil.ConnectionTestUtils.checkConnection
import com.emarsys.testUtil.DatabaseTestUtils.deleteCoreDatabase
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.RetryUtils.retryRule
import com.emarsys.testUtil.TestUrls.DENNA_ECHO
import com.emarsys.testUtil.TestUrls.customResponse
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import kotlinx.coroutines.CoroutineScope
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.kotlin.*
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.CountDownLatch

class RequestManagerTest {
    private lateinit var manager: RequestManager
    private lateinit var requestModel: RequestModel
    private lateinit var shardModel: ShardModel
    private lateinit var fakeCompletionHandler: FakeCompletionHandler
    private lateinit var mockDefaultHandler: CoreCompletionHandler
    private lateinit var completionHandlerLatch: CountDownLatch
    private lateinit var runnableFactoryLatch: CountDownLatch
    private lateinit var mockConnectionWatchDog: ConnectionWatchDog
    private lateinit var coreSdkHandlerProvider: CoreSdkHandlerProvider
    private lateinit var coreSdkHandler: CoreSdkHandler
    private lateinit var uiHandler: Handler
    private lateinit var mockRequestRepository: Repository<RequestModel, SqlSpecification>
    private lateinit var mockShardRepository: Repository<ShardModel, SqlSpecification>
    private lateinit var worker: Worker
    private lateinit var timestampProvider: TimestampProvider
    private lateinit var uuidProvider: UUIDProvider
    private lateinit var mockRestClient: RestClient
    private lateinit var mockCallbackRegistry: Registry<RequestModel, CompletionListener?>
    private lateinit var coreCompletionHandlerMiddlewareProvider: CoreCompletionHandlerMiddlewareProvider
    private lateinit var mockRequestModelMapper: Mapper<RequestModel, RequestModel>
    private lateinit var mockCompletionHandlerProxyProvider: CompletionHandlerProxyProvider
    private lateinit var mockScopeDelegatorCompletionHandlerProvider: ScopeDelegatorCompletionHandlerProvider
    private lateinit var mockScope: CoroutineScope

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Rule
    @JvmField
    var retry: TestRule = retryRule

    @Before
    fun init() {
        deleteCoreDatabase()
        val requestModelMappers: MutableList<Mapper<RequestModel, RequestModel>?> = ArrayList()
        mockRequestModelMapper = mock()
        requestModelMappers.add(mockRequestModelMapper)
        whenever(mockRequestModelMapper.map(any())).thenAnswer { invocation ->
            val args = invocation.arguments
            args[0]
        }
        val context = getTargetContext()
        checkConnection(context)
        coreSdkHandlerProvider = CoreSdkHandlerProvider()
        coreSdkHandler = coreSdkHandlerProvider.provideHandler()
        uiHandler = Handler(Looper.getMainLooper())
        mockConnectionWatchDog = mock()
        mockRequestRepository = mock {
            on { isEmpty() } doReturn true
        }
        mockShardRepository = mock()
        mockCallbackRegistry = mock()
        completionHandlerLatch = CountDownLatch(1)
        fakeCompletionHandler = FakeCompletionHandler(completionHandlerLatch)
        mockDefaultHandler = mock()
        val restClient = RestClient(
            ConnectionProvider(),
            mock(),
            mock(),
            requestModelMappers,
            Handler(Looper.getMainLooper()),
            CoreSdkHandlerProvider().provideHandler()
        )
        mockRestClient = mock()
        coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(
            mockRequestRepository,
            uiHandler,
            coreSdkHandler
        )
        worker = DefaultWorker(
            mockRequestRepository,
            mockConnectionWatchDog,
            uiHandler,
            fakeCompletionHandler,
            restClient,
            coreCompletionHandlerMiddlewareProvider
        )
        mockCompletionHandlerProxyProvider = mock {
            on { provideProxy(isNull(), any()) } doReturn mockDefaultHandler
        }
        mockScopeDelegatorCompletionHandlerProvider = mock {
            on { provide(any(), any()) } doReturn fakeCompletionHandler
        }
        mockScope = mock()
        manager = RequestManager(
            coreSdkHandler,
            mockRequestRepository,
            mockShardRepository,
            worker,
            mockRestClient,
            mockCallbackRegistry,
            mockDefaultHandler,
            mockCompletionHandlerProxyProvider,
            mockScopeDelegatorCompletionHandlerProvider,
            mockScope
        )
        timestampProvider = TimestampProvider()
        uuidProvider = UUIDProvider()
        runnableFactoryLatch = CountDownLatch(1)
        manager.runnableFactory = FakeRunnableFactory(runnableFactoryLatch)
        val headers: MutableMap<String, String> = HashMap()
        headers["accept"] = "application/json"
        headers["content"] = "application/x-www-form-urlencoded"
        requestModel = RequestModel.Builder(timestampProvider, uuidProvider)
            .url(DENNA_ECHO)
            .method(RequestMethod.GET)
            .headers(headers)
            .build()
        shardModel = ShardModel(
            "shard_id",
            "shard_type",
            HashMap(),
            0, Long.MAX_VALUE
        )
    }

    @After
    fun tearDown() {
        coreSdkHandler.looper.quit()
    }

    @Test
    fun testSubmit_shouldAddRequestModelToQueue() {
        manager.submit(requestModel, null)
        runnableFactoryLatch.await()
        verify(mockRequestRepository).add(requestModel)
    }

    @Test
    fun testSubmit_withRequestModel_shouldInvokeRunOnTheWorker() {
        val worker = mock<Worker>()
        ReflectionTestUtils.setInstanceField(manager, "worker", worker)
        manager.submit(requestModel, null)
        runnableFactoryLatch.await()
        verify(worker).run()
    }

    @Test
    fun testSubmit_withRequestModel_executesRunnableOn_CoreSDKHandlerThread() {
        val fakeRunnableFactory = FakeRunnableFactory(runnableFactoryLatch, true)
        manager.runnableFactory = fakeRunnableFactory
        manager.submit(requestModel, null)
        runnableFactoryLatch.await()
        Assert.assertEquals(1, fakeRunnableFactory.executionCount.toLong())
    }

    @Test
    fun testSubmit_withRequestModel_Success() {
        whenever(mockConnectionWatchDog.isConnected).thenReturn(true, false)
        whenever(mockRequestRepository.isEmpty()).thenReturn(false, false, true)
        whenever(
            mockRequestRepository.query(any())
        ).thenReturn(listOf(requestModel), emptyList())
        manager.submit(requestModel, null)
        completionHandlerLatch.await()
        Assert.assertEquals(requestModel.id, fakeCompletionHandler.successId)
        Assert.assertEquals(1, fakeCompletionHandler.onSuccessCount.toLong())
        Assert.assertEquals(0, fakeCompletionHandler.onErrorCount.toLong())
    }

    @Test
    fun testSubmit_withRequestModel_shouldRegisterCallbackToRegistry() {
        val completionListener = mock<CompletionListener>()
        
        manager.submit(requestModel, completionListener)
        runnableFactoryLatch.await()
        verify(mockCallbackRegistry).register(requestModel, completionListener)
    }

    @Test
    fun testSubmit_withRequestModel_shouldRegister_null_ToRegistryAsWell() {
        manager.submit(requestModel, null)
        runnableFactoryLatch.await()
        verify(mockCallbackRegistry).register(requestModel, null)
    }


    @Test
    fun testSubmitNow_withoutCompletionHandler_shouldCallProxyProviderForCompletionHandler() {
        whenever(mockScopeDelegatorCompletionHandlerProvider.provide(any(), any())).doReturn(mockDefaultHandler)
        manager.submitNow(requestModel)
        verify(mockScopeDelegatorCompletionHandlerProvider).provide(mockDefaultHandler, mockScope)
        verify(mockCompletionHandlerProxyProvider, times(2))
            .provideProxy(null, mockDefaultHandler)
        verify(mockRestClient).execute(requestModel, mockDefaultHandler)
    }

    @Test
    fun testSubmitNow_shouldCallProxyProviderForCompletionHandler() {
        manager.submitNow(requestModel, fakeCompletionHandler)
        verify(mockScopeDelegatorCompletionHandlerProvider).provide(fakeCompletionHandler, mockScope)
        verify(mockCompletionHandlerProxyProvider).provideProxy(null, fakeCompletionHandler)
        verify(mockRestClient).execute(requestModel, mockDefaultHandler)
    }

    @Test
    fun testSubmitNow_shouldCallProxyProviderForCompletionHandler_withScope() {
        val mockOtherScope: CoroutineScope = mock()
        manager.submitNow(requestModel, fakeCompletionHandler, mockOtherScope)
        verify(mockScopeDelegatorCompletionHandlerProvider).provide(fakeCompletionHandler, mockOtherScope)
        verify(mockCompletionHandlerProxyProvider).provideProxy(null, fakeCompletionHandler)
        verify(mockRestClient).execute(requestModel, mockDefaultHandler)
    }

    @Test
    fun testSubmitNow_shouldCallRestClientsExecuteWithGivenParameters() {
        manager.submitNow(requestModel, fakeCompletionHandler)
        verify(mockRestClient).execute(requestModel, mockDefaultHandler)
    }

    @Test
    fun testSubmitNow_shouldCallRestClient_withDefaultHandler() {
        manager.submitNow(requestModel)
        verify(mockRestClient).execute(requestModel, mockDefaultHandler)
    }

    @Test
    fun testError_callbackWithResponseContainsRequestModel() {
        requestModel =
            RequestModel.Builder(timestampProvider, uuidProvider).url(customResponse(405))
                .method(RequestMethod.GET).build()
        whenever(mockConnectionWatchDog.isConnected).thenReturn(true, false)
        whenever(mockRequestRepository.isEmpty()).thenReturn(false, false, true)
        whenever(
            mockRequestRepository.query(any())).thenReturn(listOf(requestModel), emptyList())
        manager.submit(requestModel, null)
        completionHandlerLatch.await()
        Assert.assertEquals(requestModel.id, fakeCompletionHandler.errorId)
        Assert.assertEquals(0, fakeCompletionHandler.onSuccessCount.toLong())
        Assert.assertEquals(1, fakeCompletionHandler.onErrorCount.toLong())
        Assert.assertEquals(405, fakeCompletionHandler.failureResponseModel.statusCode.toLong())
    }

    @Test
    fun testError_withRequestModel_callbackWithException() {
        requestModel = RequestModel.Builder(timestampProvider, uuidProvider)
            .url("https://www.nosuchwebsite.emarsys.com").method(RequestMethod.GET).build()
        whenever(mockConnectionWatchDog.isConnected).thenReturn(true, false)
        whenever(mockRequestRepository.isEmpty()).thenReturn(false, false, true)
        whenever(mockRequestRepository.query(any())).thenReturn(listOf(requestModel), emptyList())
        manager.submit(requestModel, null)
        completionHandlerLatch.await()
        Assert.assertEquals(requestModel.id, fakeCompletionHandler.errorId)
        Assert.assertEquals(0, fakeCompletionHandler.onSuccessCount.toLong())
        Assert.assertEquals(1, fakeCompletionHandler.onErrorCount.toLong())
        Assert.assertEquals(
            (UnknownHostException() as Exception).javaClass,
            fakeCompletionHandler.exception.javaClass
        )
    }

    @Test
    fun testSubmit_shouldAddShardModelToDatabase() {
        manager.submit(shardModel)
        runnableFactoryLatch.await()
        verify(mockShardRepository).add(shardModel)
    }

    @Test
    fun testSubmit_withShardModel_executesRunnableOn_CoreSDKHandlerThread() {
        val fakeRunnableFactory = FakeRunnableFactory(runnableFactoryLatch, true)
        manager.runnableFactory = fakeRunnableFactory
        manager.submit(shardModel)
        runnableFactoryLatch.await()
        Assert.assertEquals(1, fakeRunnableFactory.executionCount.toLong())
    }
}