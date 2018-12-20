package com.emarsys.core.request

import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.fake.FakeCompletionHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestResult
import com.emarsys.core.request.model.asRequestResult
import com.emarsys.core.testUtil.RequestModelTestUtils
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TestUrls
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

class RestClientTest {

    private lateinit var client: RestClient
    private lateinit var latch: CountDownLatch

    private lateinit var timestampProvider: TimestampProvider
    private lateinit var connectionProvider: ConnectionProvider

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule

    @Suppress("UNCHECKED_CAST")
    @Before
    fun setup() {
        ConnectionTestUtils.checkConnection(InstrumentationRegistry.getTargetContext())

        timestampProvider = mock(TimestampProvider::class.java)
        connectionProvider = ConnectionProvider()
        client = RestClient(connectionProvider, timestampProvider)
        latch = CountDownLatch(1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_connectionProvider_mustNotBeNull() {
        RestClient(null, timestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timestampProvider_mustNotBeNull() {
        RestClient(connectionProvider, null)
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