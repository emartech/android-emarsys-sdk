package com.emarsys.core.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mapper
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

class RequestTaskTest {
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockCoreCompletionHandler: CoreCompletionHandler
    private lateinit var connectionProvider: ConnectionProvider
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockResponseHandlersProcessor: ResponseHandlersProcessor
    private lateinit var requestModelMappers: MutableList<Mapper<RequestModel, RequestModel>>
    private lateinit var coreSdkHandler: CoreSdkHandler
    private lateinit var fakeCoreCompletionHandler: CoreCompletionHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule


    companion object {
        private const val WRONG_URL = "https://localhost/missing"
        private const val URL = "https://emarsys.com"
        private const val TIMESTAMP_1: Long = 600
        private const val TIMESTAMP_2: Long = 1600
    }


    @Before
    fun setUp() {
        mockRequestModel = mock()
        mockCoreCompletionHandler = mock()
        connectionProvider = ConnectionProvider()
        mockTimestampProvider = mock()
        mockResponseHandlersProcessor = mock()
        requestModelMappers = ArrayList()
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        fakeCoreCompletionHandler =
                object : CoreCompletionHandler {
                    override fun onSuccess(id: String?, responseModel: ResponseModel?) {
                        Thread.currentThread().name.startsWith("CoreSDKHandlerThread") shouldBe true
                    }

                    override fun onError(id: String?, responseModel: ResponseModel?) {
                        Thread.currentThread().name.startsWith("CoreSDKHandlerThread") shouldBe true
                    }

                    override fun onError(id: String?, cause: Exception?) {
                        Thread.currentThread().name.startsWith("CoreSDKHandlerThread") shouldBe true
                    }
                }

        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(TIMESTAMP_1, TIMESTAMP_2)
    }

    @Test
    @Throws(IOException::class)
    fun testDoInBackground_shouldBeResilientToRuntimeExceptions() {
        connectionProvider = mock()
        val requestModel: RequestModel = mock()
        whenever(requestModel.url).thenReturn(URL(WRONG_URL))
        val runtimeException: Exception = RuntimeException("Sneaky exception")
        val connection: HttpsURLConnection = mock()
        doThrow(runtimeException).`when`(connection).connect()
        whenever(connectionProvider.provideConnection(requestModel)).thenReturn(connection)

        val requestTask = createRequestTask(requestModel)

        try {
            requestTask.doInBackground()
        } catch (e: Exception) {
            Assert.fail("Request Task should handle exception: " + e.message)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testDoInBackground_mappersHaveBeenCalled() {
        connectionProvider = mock()
        val requestModel: RequestModel = mock()
        whenever(requestModel.url).thenReturn(URL(URL))
        val connection: HttpsURLConnection = mock()
        val mapper1: Mapper<RequestModel, RequestModel> = mock()
        val mapper2: Mapper<RequestModel, RequestModel> = mock()
        val expectedRequestModel1: RequestModel = mock()
        val expectedRequestModel2: RequestModel = mock()
        requestModelMappers.add(mapper1)
        requestModelMappers.add(mapper2)
        whenever(mapper1.map(requestModel)).thenReturn(expectedRequestModel1)
        whenever(mapper2.map(expectedRequestModel1)).thenReturn(expectedRequestModel2)
        whenever(connectionProvider.provideConnection(expectedRequestModel2)).thenReturn(connection)

        val requestTask = createRequestTask(requestModel)

        requestTask.doInBackground()
        verify(mapper1).map(requestModel)
        verify(mapper2).map(expectedRequestModel1)
        verify(connectionProvider).provideConnection(expectedRequestModel2)
    }

    @Test
    fun testOnPostExecute_shouldRunOnCoreSdkThread_whenSuccess() {
        val latch = CountDownLatch(1)
        val mockResponseModel: ResponseModel = mock {
            on { statusCode } doReturn 200
        }

        val requestTask = createRequestTask()

        ReflectionTestUtils.setInstanceField(requestTask, "responseModel", mockResponseModel)
        ReflectionTestUtils.invokeInstanceMethod<RequestTask>(
                requestTask,
                "onPostExecute",
                Pair(Void::class.java, null)
        )
        coreSdkHandler.post {
            latch.countDown()
        }

        latch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun testOnPostExecute_shouldRunOnCoreSdkThread_whenError() {
        val latch = CountDownLatch(1)
        val mockResponseModel: ResponseModel = mock()

        val requestTask = createRequestTask()

        ReflectionTestUtils.setInstanceField(requestTask, "responseModel", mockResponseModel)
        ReflectionTestUtils.invokeInstanceMethod<RequestTask>(
                requestTask,
                "onPostExecute",
                Pair(Void::class.java, null)
        )
        coreSdkHandler.post {
            latch.countDown()
        }

        latch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun testOnPostExecute_shouldRunOnCoreSdkThread_whenException() {
        val latch = CountDownLatch(1)
        val mockResponseModel: ResponseModel = mock()
        val mockException: Exception = mock()

        val requestTask = createRequestTask()

        ReflectionTestUtils.setInstanceField(requestTask, "responseModel", mockResponseModel)
        ReflectionTestUtils.setInstanceField(requestTask, "exception", mockException)
        ReflectionTestUtils.invokeInstanceMethod<RequestTask>(
                requestTask,
                "onPostExecute",
                Pair(Void::class.java, null)
        )
        coreSdkHandler.post {
            latch.countDown()
        }

        latch.await(2, TimeUnit.SECONDS)
    }

    private fun createRequestTask(requestModel: RequestModel = mock()): RequestTask {
        return RequestTask(
                requestModel,
                fakeCoreCompletionHandler,
                connectionProvider,
                mockTimestampProvider,
                mockResponseHandlersProcessor,
                requestModelMappers,
                coreSdkHandler
        )
    }
}