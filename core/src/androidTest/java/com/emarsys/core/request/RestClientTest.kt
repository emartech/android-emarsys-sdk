package com.emarsys.core.request

import com.emarsys.core.Mapper
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.fake.FakeCompletionHandler
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestResult
import com.emarsys.core.request.model.asRequestResult
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.testUtil.RequestModelTestUtils
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TestUrls
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

class RestClientTest : AnnotationSpec() {

    private lateinit var client: RestClient
    private lateinit var latch: CountDownLatch

    private lateinit var connectionProvider: ConnectionProvider
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockResponseHandlersProcessor: ResponseHandlersProcessor
    private lateinit var mockRequestModelMapper: Mapper<RequestModel, RequestModel>
    private lateinit var requestModelMappers: List<Mapper<RequestModel, RequestModel>>
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @Suppress("UNCHECKED_CAST")
    @Before
    fun setup() {
        ConnectionTestUtils.checkConnection(InstrumentationRegistry.getTargetContext())

        mockTimestampProvider = mock()
        connectionProvider = ConnectionProvider()
        mockResponseHandlersProcessor = mock()
        mockRequestModelMapper = mock() as Mapper<RequestModel, RequestModel>
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()

        whenever(mockRequestModelMapper.map(any())).thenAnswer { invocation ->
            val args = invocation.arguments
            args[0]
        }

        requestModelMappers = listOf(mockRequestModelMapper)
        client = RestClient(
            connectionProvider,
            mockTimestampProvider,
            mockResponseHandlersProcessor,
            requestModelMappers,
            concurrentHandlerHolder
        )
        latch = CountDownLatch(1)
    }

    @Test
    fun testSendRequest_requestDoneSuccessfully() {
        val handler = FakeCompletionHandler(latch)
        val model = RequestModelTestUtils.createRequestModel(RequestMethod.GET)

        client.execute(model, handler)

        latch.await()

        handler.asRequestResult() shouldBe RequestResult.success(model.id)
    }

    @Test
    fun testSendRequest_callbackWithResponseModel() {
        val handler = FakeCompletionHandler(latch)
        val model = RequestModelTestUtils.createRequestModel(url = TestUrls.customResponse(405))

        client.execute(model, handler)

        latch.await()

        handler.asRequestResult() shouldBe RequestResult.failure(model.id, 405)
    }

    @Test
    fun testSendRequest_callbackWithException() {
        val handler = FakeCompletionHandler(latch)
        val timestampProvider = TimestampProvider()
        val model = RequestModel.Builder(timestampProvider, UUIDProvider()).url("https://www.nosuchwebsite.emarsys.com").method(RequestMethod.GET).build()

        client.execute(model, handler)

        latch.await()

        handler.asRequestResult() shouldBe RequestResult.failure(model.id, UnknownHostException::class.java)
    }

    @Test
    fun testExecute_mappersHaveBeenCalled() {
        connectionProvider = mock()
        val requestModel: RequestModel = mock ()
        val expectedRequestModel1: RequestModel = mock()
        val expectedRequestModel2: RequestModel = mock()
        val mockRequestModelMapper1: Mapper<RequestModel, RequestModel> = mock {
            on { map(requestModel) } doReturn expectedRequestModel1
        }
        val mockRequestModelMapper2: Mapper<RequestModel, RequestModel> = mock {
            on { map(expectedRequestModel1) } doReturn expectedRequestModel2
        }

        requestModelMappers = listOf(mockRequestModelMapper1, mockRequestModelMapper2)
        client = RestClient(
            connectionProvider,
            mockTimestampProvider,
            mockResponseHandlersProcessor,
            requestModelMappers,
            concurrentHandlerHolder
        )

        client.execute(requestModel, mock())
        verify(mockRequestModelMapper1).map(requestModel)
        verify(mockRequestModelMapper2).map(expectedRequestModel1)
    }
}