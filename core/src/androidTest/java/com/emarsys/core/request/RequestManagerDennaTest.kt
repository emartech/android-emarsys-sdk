package com.emarsys.core.request

import com.emarsys.core.Mapper
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.fake.FakeCompletionHandler
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.ShardModelRepository
import com.emarsys.core.worker.DefaultWorker
import com.emarsys.core.worker.Worker
import com.emarsys.testUtil.ConnectionTestUtils.checkConnection
import com.emarsys.testUtil.DatabaseTestUtils.deleteCoreDatabase
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.TestUrls.DENNA_ECHO
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch

class RequestManagerDennaTest  {
    private lateinit var manager: RequestManager
    private lateinit var headers: MutableMap<String, String>
    private lateinit var model: RequestModel
    private lateinit var latch: CountDownLatch
    private lateinit var fakeCompletionHandler: FakeCompletionHandler
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var worker: Worker
    private lateinit var timestampProvider: TimestampProvider
    private lateinit var uuidProvider: UUIDProvider
    private lateinit var coreCompletionHandlerMiddlewareProvider: CoreCompletionHandlerMiddlewareProvider


    private lateinit var mockRequestModelMapper: Mapper<RequestModel, RequestModel>

    @Before
    fun init() {
        deleteCoreDatabase()
        val requestModelMappers: MutableList<Mapper<RequestModel, RequestModel>> = mutableListOf()
        mockRequestModelMapper = mock {
            on { map(any()) } doAnswer { invocation ->
                val args = invocation.arguments
                args[0] as RequestModel
            }
        }
        requestModelMappers.add(mockRequestModelMapper)
        val context = getTargetContext()
        checkConnection(context)
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        val connectionWatchDog = ConnectionWatchDog(context, concurrentHandlerHolder)
        val coreDbHelper = CoreDbHelper(context, mutableMapOf())
        val requestRepository: Repository<RequestModel, SqlSpecification> =
            RequestModelRepository(coreDbHelper, concurrentHandlerHolder)
        val shardRepository: Repository<ShardModel, SqlSpecification> =
            ShardModelRepository(coreDbHelper, concurrentHandlerHolder)
        latch = CountDownLatch(1)
        fakeCompletionHandler = FakeCompletionHandler(latch)
        val restClient = RestClient(
            ConnectionProvider(),
            mock(),
            mock(),
            requestModelMappers.toList(),
            concurrentHandlerHolder
        )
        coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(
            requestRepository,
            concurrentHandlerHolder
        )
        worker = DefaultWorker(
            requestRepository,
            connectionWatchDog,
            concurrentHandlerHolder,
            fakeCompletionHandler,
            restClient,
            coreCompletionHandlerMiddlewareProvider
        )
        timestampProvider = TimestampProvider()
        uuidProvider = UUIDProvider()
        manager = RequestManager(
            concurrentHandlerHolder,
            requestRepository,
            shardRepository,
            worker as DefaultWorker,
            restClient,
            mock(),
            fakeCompletionHandler,
            mock(),
            mock()
        )
        headers = HashMap()
        headers["Accept"] = "application/json"
        headers["Content"] = "application/x-www-form-urlencoded"
        headers["Header1"] = "value1"
        headers["Header2"] = "value2"
    }

    @After
    fun tearDown() {
        concurrentHandlerHolder.coreLooper.quit()
    }

    @Test
    fun testGet() {
        model = RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO)
            .method(RequestMethod.GET).headers(
                headers
            ).build()

        manager.submit(model, null)
        latch.await()

        fakeCompletionHandler.exception shouldBe null
        fakeCompletionHandler.onErrorCount.toLong() shouldBe 0
        fakeCompletionHandler.onSuccessCount.toLong() shouldBe 1
        fakeCompletionHandler.successResponseModel.statusCode.toLong() shouldBe 200
        val responseJson = JSONObject(fakeCompletionHandler.successResponseModel.body!!)
        val headers = responseJson["headers"] as JSONObject
        headers["Header1".lowercase()] shouldBe "value1"
        headers["Header2".lowercase()] shouldBe "value2"
        headers["Accept".lowercase()] shouldBe "application/json"
        headers["Content".lowercase()] shouldBe "application/x-www-form-urlencoded"
        responseJson["method"] shouldBe "GET"

        responseJson.has("body") shouldBe false
    }

    @Test
    fun testPost() {
        val deepPayload = HashMap<String, Any>()
        deepPayload["deep1"] = "deepValue1"
        deepPayload["deep2"] = "deepValue2"
        val payload = HashMap<String, Any?>()
        payload["key1"] = "val1"
        payload["key2"] = "val2"
        payload["key3"] = "val3"
        payload["key4"] = 4
        payload["deepKey"] = deepPayload
        model = RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO)
            .method(RequestMethod.POST).headers(
                headers
            ).payload(payload).build()

        manager.submit(model, null)
        latch.await()

        fakeCompletionHandler.exception shouldBe null
        fakeCompletionHandler.onErrorCount.toLong() shouldBe 0
        fakeCompletionHandler.onSuccessCount.toLong() shouldBe 1
        fakeCompletionHandler.successResponseModel.statusCode.toLong() shouldBe 200
        val responseJson = JSONObject(fakeCompletionHandler.successResponseModel.body!!)
        val headers = responseJson.getJSONObject("headers")
        val body = responseJson.getJSONObject("body")
        headers["Header1".lowercase()] shouldBe "value1"
        headers["Header2".lowercase()] shouldBe "value2"
        headers["Accept".lowercase()] shouldBe "application/json"
        headers["Content".lowercase()] shouldBe "application/x-www-form-urlencoded"
        responseJson["method"] shouldBe "POST"
        body["key1"] shouldBe "val1"
        body["key2"] shouldBe "val2"
        body["key3"] shouldBe "val3"
        body["key4"] shouldBe 4
        val soDeepJson = body.getJSONObject("deepKey")
        soDeepJson.getString("deep1") shouldBe "deepValue1"
        soDeepJson.getString("deep2") shouldBe "deepValue2"
    }

    @Test
    fun testPut() {
        model = RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO)
            .method(RequestMethod.PUT).headers(
                headers
            ).build()
        manager.submit(model, null)
        latch.await()
        fakeCompletionHandler.exception shouldBe null
        fakeCompletionHandler.onErrorCount.toLong() shouldBe 0
        fakeCompletionHandler.onSuccessCount.toLong() shouldBe 1
        fakeCompletionHandler.successResponseModel.statusCode.toLong() shouldBe 200
        val responseJson = JSONObject(fakeCompletionHandler.successResponseModel!!.body!!)
        val headers = responseJson.getJSONObject("headers")
        headers["Header1".lowercase()] shouldBe "value1"
        headers["Header2".lowercase()] shouldBe "value2"
        headers["Accept".lowercase()] shouldBe "application/json"
        headers["Content".lowercase()] shouldBe "application/x-www-form-urlencoded"
        responseJson["method"] shouldBe "PUT"
        responseJson.has("body") shouldBe false
    }

    @Test
    fun testDelete() {
        model = RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO)
            .method(RequestMethod.DELETE).headers(
                headers
            ).build()
        manager.submit(model, null)
        latch.await()
        fakeCompletionHandler.exception shouldBe null
        fakeCompletionHandler.onErrorCount.toLong() shouldBe 0
        fakeCompletionHandler.onSuccessCount.toLong() shouldBe 1
        fakeCompletionHandler.successResponseModel.statusCode.toLong() shouldBe 200
        val responseJson = JSONObject(fakeCompletionHandler.successResponseModel!!.body!!)
        val headers = responseJson.getJSONObject("headers")
        headers["Header1".lowercase()] shouldBe "value1"
        headers["Header2".lowercase()] shouldBe "value2"
        headers["Accept".lowercase()] shouldBe "application/json"
        headers["Content".lowercase()] shouldBe "application/x-www-form-urlencoded"
        responseJson["method"] shouldBe "DELETE"
        responseJson.has("body") shouldBe false
    }
}