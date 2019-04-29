package com.emarsys.core.request

import com.emarsys.core.Mapper
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.fake.FakeCompletionHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestResult
import com.emarsys.core.request.model.asRequestResult
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.testUtil.RequestModelTestUtils
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TestUrls
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

class RestClientTest {

    private lateinit var client: RestClient
    private lateinit var latch: CountDownLatch

    private lateinit var connectionProvider: ConnectionProvider
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockResponseHandlersProcessor: ResponseHandlersProcessor
    private lateinit var mockRequestModelMapper: Mapper<RequestModel, RequestModel>
    private lateinit var requestModelMappers: List<Mapper<RequestModel, RequestModel>>

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Suppress("UNCHECKED_CAST")
    @Before
    fun setup() {
        ConnectionTestUtils.checkConnection(InstrumentationRegistry.getTargetContext())

        mockTimestampProvider = mock(TimestampProvider::class.java)
        connectionProvider = ConnectionProvider()
        mockResponseHandlersProcessor = mock(ResponseHandlersProcessor::class.java)
        mockRequestModelMapper = mock(Mapper::class.java) as Mapper<RequestModel, RequestModel>

        whenever(mockRequestModelMapper.map(any<RequestModel>(RequestModel::class.java))).thenAnswer { invocation ->
            val args = invocation.arguments
            args[0]
        }

        requestModelMappers = listOf(mockRequestModelMapper)
        client = RestClient(connectionProvider, mockTimestampProvider, mockResponseHandlersProcessor, requestModelMappers)
        latch = CountDownLatch(1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_connectionProvider_mustNotBeNull() {
        RestClient(null, mockTimestampProvider, mockResponseHandlersProcessor, requestModelMappers)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timestampProvider_mustNotBeNull() {
        RestClient(connectionProvider, null, mockResponseHandlersProcessor, requestModelMappers)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_responseHandlersRunner_mustNotBeNull() {
        RestClient(connectionProvider, mockTimestampProvider, null, requestModelMappers)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestModelMapper_mustNotBeNull() {
        RestClient(connectionProvider, mockTimestampProvider, mockResponseHandlersProcessor, null)
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

}