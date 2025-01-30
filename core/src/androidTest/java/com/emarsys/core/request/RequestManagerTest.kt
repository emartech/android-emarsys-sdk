package com.emarsys.core.request

import android.os.Handler
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mapper
import com.emarsys.core.Registry
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.fake.FakeCompletionHandler
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.worker.DefaultWorker
import com.emarsys.core.worker.DelegatorCompletionHandlerProvider
import com.emarsys.core.worker.Worker
import com.emarsys.testUtil.ConnectionTestUtils.checkConnection
import com.emarsys.testUtil.DatabaseTestUtils.deleteCoreDatabase
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.RetryUtils
import com.emarsys.testUtil.TestUrls.DENNA_ECHO
import com.emarsys.testUtil.TestUrls.customResponse
import com.emarsys.testUtil.mockito.ThreadSpy
import com.emarsys.testUtil.rules.RetryRule
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

class RequestManagerTest  {

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

    private lateinit var manager: RequestManager
    private lateinit var requestModel: RequestModel
    private lateinit var shardModel: ShardModel
    private lateinit var fakeCompletionHandler: FakeCompletionHandler
    private lateinit var mockDefaultHandler: CoreCompletionHandler
    private lateinit var completionHandlerLatch: CountDownLatch
    private lateinit var runnableFactoryLatch: CountDownLatch
    private lateinit var mockConnectionWatchDog: ConnectionWatchDog
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
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
    private lateinit var mockDelegatorCompletionHandlerProvider: DelegatorCompletionHandlerProvider
    private lateinit var callbackRegistryThreadSpy: ThreadSpy<Registry<RequestModel, CompletionListener?>>
    private lateinit var shardRepositoryThreadSpy: ThreadSpy<Repository<ShardModel, SqlSpecification>>

    @Before
    fun setUp() {
        deleteCoreDatabase()
        val requestModelMappers: MutableList<Mapper<RequestModel, RequestModel>> = mutableListOf()
        mockRequestModelMapper = mock()
        requestModelMappers.add(mockRequestModelMapper)
        whenever(mockRequestModelMapper.map(any())).thenAnswer { invocation ->
            val args = invocation.arguments
            args[0]
        }
        val context = getTargetContext()
        checkConnection(context)
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockConnectionWatchDog = mock()
        mockRequestRepository = mock {
            on { isEmpty() } doReturn true
        }
        callbackRegistryThreadSpy = ThreadSpy()
        shardRepositoryThreadSpy = ThreadSpy()

        mockShardRepository = mock()

        runBlocking {
            whenever(mockShardRepository.add(any())).thenAnswer(shardRepositoryThreadSpy)
        }
        mockCallbackRegistry = mock {
            on { register(any(), isNull()) }.doAnswer(callbackRegistryThreadSpy)
        }

        completionHandlerLatch = CountDownLatch(1)
        fakeCompletionHandler = FakeCompletionHandler(completionHandlerLatch)
        mockDefaultHandler = mock()
        val restClient = RestClient(
            ConnectionProvider(),
            mock(),
            mock(),
            requestModelMappers.toList(),
            concurrentHandlerHolder
        )
        mockRestClient = mock()
        coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(
            mockRequestRepository,
            concurrentHandlerHolder
        )
        worker = DefaultWorker(
            mockRequestRepository,
            mockConnectionWatchDog,
            concurrentHandlerHolder,
            fakeCompletionHandler,
            restClient,
            coreCompletionHandlerMiddlewareProvider
        )
        mockCompletionHandlerProxyProvider = mock {
            on { provideProxy(isNull(), any()) } doReturn mockDefaultHandler
        }
        mockDelegatorCompletionHandlerProvider = mock {
            on { provide(any(), any()) } doReturn fakeCompletionHandler
        }
        manager = RequestManager(
            concurrentHandlerHolder,
            mockRequestRepository,
            mockShardRepository,
            worker,
            mockRestClient,
            mockCallbackRegistry,
            mockDefaultHandler,
            mockCompletionHandlerProxyProvider,
            mockDelegatorCompletionHandlerProvider
        )
        timestampProvider = TimestampProvider()
        uuidProvider = UUIDProvider()
        runnableFactoryLatch = CountDownLatch(1)
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
        concurrentHandlerHolder.coreLooper.quit()
    }

    @Test

    fun testSubmit_shouldAddRequestModelToQueue() {
        manager.submit(
            requestModel,
            null
        )
        runBlocking {
            verify(mockRequestRepository, timeout(100)).add(requestModel)
        }
    }

    @Test

    fun testSubmit_withRequestModel_shouldInvokeRunOnTheWorker() {
        val worker = mock<Worker>()
        ReflectionTestUtils.setInstanceField(manager, "worker", worker)

        manager.submit(requestModel, null)

        verify(worker, timeout(100)).run()

    }

    @Test

    fun testSubmit_withRequestModel_executesRunnableOn_CoreSDKHandlerThread() {
        runBlocking {
            withContext(Dispatchers.IO) {
                manager.submit(requestModel, null)
            }
        }

        callbackRegistryThreadSpy.verifyCalledOnCoreSdkThread()
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

        fakeCompletionHandler.successId shouldBe requestModel.id
        fakeCompletionHandler.onSuccessCount.toLong() shouldBe 1
        fakeCompletionHandler.onErrorCount shouldBe 0
    }

    @Test

    fun testSubmit_withRequestModel_shouldRegisterCallbackToRegistry() {
        val completionListener = mock<CompletionListener>()

        manager.submit(requestModel, completionListener)
        verify(mockCallbackRegistry, timeout(100)).register(requestModel, completionListener)

    }

    @Test

    fun testSubmit_withRequestModel_shouldRegister_null_ToRegistryAsWell() {
        manager.submit(requestModel, null)

        verify(mockCallbackRegistry, timeout(100)).register(requestModel, null)
    }


    @Test

    fun testSubmitNow_withoutCompletionHandler_shouldCallProxyProviderForCompletionHandler() {
        whenever(mockDelegatorCompletionHandlerProvider.provide(any(), any())).doReturn(
            mockDefaultHandler
        )
        manager.submitNow(requestModel)
        verify(mockDelegatorCompletionHandlerProvider).provide(
            concurrentHandlerHolder.coreHandler.handler,
            mockDefaultHandler
        )
        verify(mockCompletionHandlerProxyProvider, times(2))
            .provideProxy(null, mockDefaultHandler)
        verify(mockRestClient).execute(requestModel, mockDefaultHandler)
    }

    @Test

    fun testSubmitNow_shouldCallProxyProviderForCompletionHandler() {
        manager.submitNow(requestModel, fakeCompletionHandler)
        verify(mockDelegatorCompletionHandlerProvider).provide(
            concurrentHandlerHolder.coreHandler.handler,
            fakeCompletionHandler
        )
        verify(mockCompletionHandlerProxyProvider).provideProxy(null, fakeCompletionHandler)
        verify(mockRestClient).execute(requestModel, mockDefaultHandler)
    }

    @Test

    fun testSubmitNow_shouldCallProxyProviderForCompletionHandler_withScope() {
        val mockOtherHandler: Handler = mock()
        manager.submitNow(requestModel, fakeCompletionHandler, mockOtherHandler)
        verify(mockDelegatorCompletionHandlerProvider).provide(
            mockOtherHandler,
            fakeCompletionHandler
        )
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
            mockRequestRepository.query(any())
        ).thenReturn(listOf(requestModel), emptyList())
        manager.submit(requestModel, null)
        completionHandlerLatch.await()

        fakeCompletionHandler.errorId shouldBe requestModel.id
        fakeCompletionHandler.onSuccessCount.toLong() shouldBe 0
        fakeCompletionHandler.onErrorCount shouldBe 1
        fakeCompletionHandler.failureResponseModel.statusCode.toLong() shouldBe 405
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

        fakeCompletionHandler.errorId shouldBe requestModel.id
        fakeCompletionHandler.onSuccessCount.toLong() shouldBe 0
        fakeCompletionHandler.onErrorCount shouldBe 1
        fakeCompletionHandler.exception.javaClass shouldBe UnknownHostException().javaClass

    }

    @Test

    fun testSubmit_shouldAddShardModelToDatabase() {
        manager.submit(shardModel)
        runBlocking {
            verify(mockShardRepository, timeout(100)).add(shardModel)
        }
    }

    @Test

    fun testSubmit_withShardModel_executesRunnableOn_CoreSDKHandlerThread() {
        runBlocking {
            withContext(Dispatchers.IO) {
                manager.submit(shardModel)
            }
        }
        shardRepositoryThreadSpy.verifyCalledOnCoreSdkThread()
    }
}