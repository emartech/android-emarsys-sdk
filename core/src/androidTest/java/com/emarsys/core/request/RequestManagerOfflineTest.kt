package com.emarsys.core.request

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionState
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.fake.FakeCompletionHandler
import com.emarsys.core.fake.FakeConnectionWatchDog
import com.emarsys.core.fake.FakeRestClient
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.core.request.model.specification.FilterByRequestIds
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.core.worker.DefaultWorker
import com.emarsys.core.worker.Worker
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import java.io.IOException
import java.util.concurrent.CountDownLatch

class RequestManagerOfflineTest  {
    companion object {
        const val URL = "https://www.emarsys.com/"
    }


    private lateinit var connectionStates: Array<Boolean>
    private lateinit var requestResults: Array<Any>
    private lateinit var requestModels: Array<RequestModel>
    private var watchDogCountDown: Int = 0
    private var completionHandlerCountDown: Int = 0

    private lateinit var watchDogLatch: CountDownLatch
    private lateinit var watchDog: FakeConnectionWatchDog
    private lateinit var requestRepository: Repository<RequestModel, SqlSpecification>
    private lateinit var shardRepository: Repository<ShardModel, SqlSpecification>
    private lateinit var completionLatch: CountDownLatch
    private lateinit var completionHandler: FakeCompletionHandler
    private lateinit var fakeRestClient: RestClient
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var worker: Worker
    private lateinit var coreCompletionHandlerMiddlewareProvider: CoreCompletionHandlerMiddlewareProvider
    private lateinit var mockProxyProvider: CompletionHandlerProxyProvider

    @Before
    fun setup() {
        watchDogCountDown = 0
        completionHandlerCountDown = 0
        DatabaseTestUtils.deleteCoreDatabase()
        requestResults = arrayOf()
        requestModels = arrayOf()
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
    }

    @After
    fun tearDown() {
        concurrentHandlerHolder.coreLooper.quit()
        concurrentHandlerHolder.networkLooper.quit()
        concurrentHandlerHolder.backgroundLooper.quit()
        DatabaseTestUtils.deleteCoreDatabase()
    }

    @Test
    fun test_online_offline_online() {
        connectionStates = arrayOf(true, false, true)
        requestResults = arrayOf(200, 200)
        requestModels = arrayOf(normal(1), normal(2))
        watchDogCountDown = 2
        completionHandlerCountDown = 1

        prepareTestCaseAndWait()

        requestRepository.isEmpty() shouldBe false
        completionHandler.latch = CountDownLatch(1)

        concurrentHandlerHolder.postOnMain {
            watchDog.connectionChangeListener?.onConnectionChanged(
                ConnectionState.CONNECTED,
                true
            )
        }

        completionHandler.latch.await()

        completionHandler.onSuccessCount shouldBe 2
        requestRepository.query(Everything()) should beEmpty()
    }

    @Test
    fun test_alwaysOnline() {
        connectionStates = arrayOf(true)
        requestResults = arrayOf(200, 200, 200)
        requestModels = arrayOf(normal(1), normal(2), normal(3))
        watchDogCountDown = 3
        completionHandlerCountDown = 3

        prepareTestCaseAndWait()

        completionHandler.onSuccessCount shouldBe 3
        requestRepository.query(Everything()) should beEmpty()
    }

    @Test
    fun test_alwaysOnline_withExpiredRequests() {
        connectionStates = arrayOf(true)
        requestResults = arrayOf(200, 200, 200)
        requestModels =
            arrayOf(normal(1), expired(1), normal(2), expired(2), expired(3), expired(4), normal(3))
        watchDogCountDown = 3
        completionHandlerCountDown = 7

        prepareTestCaseAndWait()

        completionHandler.onSuccessCount shouldBe 3
        completionHandler.onErrorCount shouldBe 4

        requestRepository.query(Everything()) should beEmpty()
    }

    @Test
    fun test_alwaysOffline() {
        connectionStates = arrayOf(false)
        requestResults = arrayOf(200, 300, 200)

        val normal1 = normal(1)
        val normal2 = normal(2)
        val normal3 = normal(3)
        requestModels = arrayOf(normal1, normal2, normal3)
        watchDogCountDown = 1
        completionHandlerCountDown = 0

        prepareTestCaseAndWait()

        completionHandler.onSuccessCount shouldBe 0
        completionHandler.onErrorCount shouldBe 0
        requestRepository.isEmpty() shouldBe false

        val result = requestRepository.query(Everything())
        result shouldBe requestModels.toList()
    }

    @Test
    fun test_4xx_doesNotStopQueue() {
        connectionStates = arrayOf(true)
        requestResults = arrayOf(200, 400, 200)
        requestModels = arrayOf(normal(1), normal(2), normal(3))
        watchDogCountDown = 3
        completionHandlerCountDown = 3

        prepareTestCaseAndWait()

        completionHandler.onSuccessCount shouldBe 2
        completionHandler.onErrorCount shouldBe 1

        requestRepository.query(Everything()) should beEmpty()
    }

    @Test
    fun test_408_stopsQueue() {
        connectionStates = arrayOf(true)
        requestResults = arrayOf(200, 408, 200)
        requestModels = arrayOf(normal(1), normal(2), normal(3))
        watchDogCountDown = 2
        completionHandlerCountDown = 1

        prepareTestCaseAndWait()

        completionHandler.onSuccessCount shouldBe 1
        completionHandler.onErrorCount shouldBe 0
        requestRepository.isEmpty() shouldBe false
    }

    @Test
    fun test_5xx_stopsQueue() {
        connectionStates = arrayOf(true)
        requestResults = arrayOf(200, 500, 200)
        requestModels = arrayOf(normal(1), normal(2), normal(3))
        watchDogCountDown = 2
        completionHandlerCountDown = 1

        prepareTestCaseAndWait()

        completionHandler.onSuccessCount shouldBe 1
        completionHandler.onErrorCount shouldBe 0
        requestRepository.isEmpty() shouldBe false
    }

    @Test
    fun test_exception_stopsQueue() {
        connectionStates = arrayOf(true)
        requestResults = arrayOf(200, 300, IOException(), 200)
        val lastNormal = normal(4)
        requestModels = arrayOf(normal(1), normal(2), normal(3), lastNormal)
        watchDogCountDown = 3
        completionHandlerCountDown = 3

        prepareTestCaseAndWait()

        completionHandler.onSuccessCount shouldBe 2
        completionHandler.onErrorCount shouldBe 1
        requestRepository.isEmpty() shouldBe false
        runBlocking {
            requestRepository.remove(FilterByRequestIds(arrayOf(lastNormal.id)))
        }

        requestRepository.query(Everything()).size shouldBe 1
    }

    @Suppress("UNCHECKED_CAST")
    private fun prepareTestCaseAndWait() {
        watchDogLatch = CountDownLatch(watchDogCountDown)
        watchDog = FakeConnectionWatchDog(watchDogLatch, *connectionStates)
        val coreDbHelper = CoreDbHelper(InstrumentationRegistry.getTargetContext(), HashMap())
        requestRepository = RequestModelRepository(coreDbHelper, concurrentHandlerHolder)
        shardRepository = ShardModelRepository(coreDbHelper, concurrentHandlerHolder)

        completionLatch = CountDownLatch(completionHandlerCountDown)
        completionHandler = FakeCompletionHandler(completionLatch)

        fakeRestClient = FakeRestClient(*requestResults)

        mockProxyProvider = mock()

        coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(
            requestRepository,
            concurrentHandlerHolder
        )
        worker = DefaultWorker(
            requestRepository,
            watchDog,
            concurrentHandlerHolder,
            completionHandler,
            fakeRestClient,
            coreCompletionHandlerMiddlewareProvider
        )

        runBlocking {
            requestModels.forEach {
                requestRepository.add(it)
            }

            worker.run()
        }

        watchDogLatch.await()
        completionLatch.await()
    }

    private fun normal(orderId: Int): RequestModel {
        val timestampProvider = TimestampProvider()
        val uuidProvider = UUIDProvider()
        return RequestModel.Builder(timestampProvider, uuidProvider).url(URL + "normal/" + orderId)
            .method(RequestMethod.GET).ttl(60000).build()
    }

    private fun expired(orderId: Int): RequestModel {
        return RequestModel(
            URL + "expired/" + orderId,
            RequestMethod.GET,
            HashMap(),
            HashMap(),
            System.currentTimeMillis() - 5000,
            100,
            UUIDProvider().provideId()
        )
    }
}
