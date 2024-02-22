package com.emarsys.core.worker

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.handler.SdkHandler
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.specification.FilterByRequestIds
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch

class CoreCompletionHandlerMiddlewareTest {
    private lateinit var coreCompletionHandler: CoreCompletionHandler
    private lateinit var requestRepository: Repository<RequestModel, SqlSpecification>
    private lateinit var mockWorker: Worker
    private lateinit var expectedId: String
    private lateinit var middleware: CoreCompletionHandlerMiddleware
    private lateinit var captor: ArgumentCaptor<Message>
    private lateinit var uiHandler: Handler
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @BeforeEach
    fun setup() {
        expectedId = "expectedId"
        mockWorker = mock()
        coreCompletionHandler = mock()
        requestRepository = mock()
        uiHandler = Handler(Looper.getMainLooper())
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()

        middleware = CoreCompletionHandlerMiddleware(
            mockWorker,
            requestRepository,
            concurrentHandlerHolder,
            coreCompletionHandler
        )
        captor = ArgumentCaptor.forClass(Message::class.java)
    }

    @Test
    fun testOnSuccess() {
        val captor = ArgumentCaptor.forClass(FilterByRequestIds::class.java)
        val expectedModel = createResponseModel(200)

        middleware.onSuccess(expectedId, expectedModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)
        runBlocking {
            verify(mockWorker).unlock()
            verify(mockWorker).run()
            verifyNoMoreInteractions(mockWorker)
            verify(requestRepository).remove(capture<FilterByRequestIds>(captor))
            val filter = captor.value

            filter.selectionArgs[0] shouldBe expectedModel.requestModel.id

            verify(coreCompletionHandler).onSuccess(expectedId, expectedModel)
        }
    }

    @Test
    fun testOnSuccess_withCompositeModel_withManyRequestModels() {
        val ids = Array(2040) { i -> "id$i" }

        val responseModel = createResponseModel(createRequestModel(ids))

        middleware.onSuccess("0", responseModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<FilterByRequestIds>().apply {
            runBlocking {
                verify(requestRepository, times(41)).remove(capture())
            }
        }

    }

    @Test
    fun testOnSuccess_callsHandlerPost() {
        middleware.concurrentHandlerHolder = concurrentHandlerHolder
        val threadSpy = ThreadSpy<Unit>()
        whenever(mockWorker.unlock()).thenAnswer(threadSpy)
        middleware.onSuccess(expectedId, createResponseModel(200))
        threadSpy.verifyCalledOnCoreSdkThread()
        verify(mockWorker, timeout(50)).run()
    }

    @Test
    fun testOnSuccess_withCompositeModel() {
        val ids = Array(2040) { i -> "id$i" }
        val responseModel = createResponseModel(createRequestModel(ids))

        middleware.onSuccess("0", responseModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<String>().apply {
            verify(middleware.coreCompletionHandler, times(2040))!!.onSuccess(
                capture(),
                eq(responseModel)
            )
            allValues shouldBe ids.toList()
        }
    }

    @Test
    fun testOnError_4xx() {
        val expectedModel = createResponseModel(403)

        middleware.onError(expectedId, expectedModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<FilterByRequestIds>().apply {
            runBlocking {
                verify(requestRepository).remove(capture())
                firstValue.selectionArgs shouldBe arrayOf(expectedModel.requestModel.id)
            }
        }

        verify(coreCompletionHandler).onError(expectedId, expectedModel)
        verify(mockWorker).unlock()
        verify(mockWorker).run()
        verifyNoMoreInteractions(mockWorker)
    }

    @Test
    fun testOnError_4xx_callsHandlerPost() {
        middleware.concurrentHandlerHolder = concurrentHandlerHolder
        val threadSpy = ThreadSpy<Unit>()
        whenever(mockWorker.unlock()).thenAnswer(threadSpy)
        middleware.onError(expectedId, createResponseModel(401))

        threadSpy.verifyCalledOnCoreSdkThread()
        verify(mockWorker, timeout(50)).run()
    }

    @Test
    fun testOnError_408_shouldHandleErrorAsRetriable() {
        val expectedModel = createResponseModel(408)

        middleware.onError(expectedId, expectedModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        verify(mockWorker).unlock()
        verifyNoMoreInteractions(mockWorker)
        verifyNoInteractions(coreCompletionHandler)
        verifyNoInteractions(requestRepository)
    }

    @Test
    fun testOnError_429_shouldHandleErrorAsRetriable() {
        val expectedModel = createResponseModel(429)

        middleware.onError(expectedId, expectedModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        verify(mockWorker).unlock()
        verifyNoMoreInteractions(mockWorker)
        verifyNoInteractions(coreCompletionHandler)
        verifyNoInteractions(requestRepository)
    }

    @Test
    fun testOnError_5xx() {
        val expectedModel = createResponseModel(500)

        middleware.onError(expectedId, expectedModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        verify(mockWorker).unlock()
        verifyNoMoreInteractions(mockWorker)
        verifyNoInteractions(coreCompletionHandler)
        verifyNoInteractions(requestRepository)
    }

    @Test
    fun testOnError_4xx_withCompositeModel() {
        val ids = Array(2040) { i -> "id$i" }
        val responseModel = ResponseModel.Builder()
            .statusCode(400)
            .message("Bad Request")
            .headers(HashMap())
            .body("{'key': 'value'}")
            .requestModel(createRequestModel(ids))
            .build()

        middleware.onError("0", responseModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<String>().apply {
            verify(middleware.coreCompletionHandler, times(2040))!!.onError(
                capture(),
                eq(responseModel)
            )
            allValues shouldBe ids.toList()
        }
    }

    @Test
    fun testOnError_4xx_withCompositeModel_withManyRequests_whenModulo500Is0() {
        val ids = Array(2000) { i -> "id$i" }

        val responseModel = ResponseModel.Builder()
            .statusCode(400)
            .message("Bad Request")
            .headers(HashMap())
            .body("{'key': 'value'}")
            .requestModel(createRequestModel(ids))
            .build()
        middleware.onError("0", responseModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<FilterByRequestIds>().apply {
            runBlocking {
                verify(requestRepository, times(40)).remove(capture())
                allValues.size shouldBeLessThanOrEqual 500
            }
        }
    }


    @Test
    fun testOnError_4xx_withCompositeModel_withManyRequests_whenModulo500IsNotZero() {
        val ids = Array(2040) { i -> "id$i" }

        val responseModel = ResponseModel.Builder()
            .statusCode(400)
            .message("Bad Request")
            .headers(HashMap())
            .body("{'key': 'value'}")
            .requestModel(createRequestModel(ids))
            .build()

        middleware.onError("0", responseModel)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<FilterByRequestIds>().apply {
            runBlocking {
                verify(requestRepository, times(41)).remove(capture())
                allValues.size shouldBeLessThanOrEqual 500
            }
        }
    }

    @Test
    fun testOnError_withException() {
        val expectedException = Exception("Expected exception")

        middleware.onError(expectedId, expectedException)

        waitForEventLoopToFinish(concurrentHandlerHolder.coreHandler)
        waitForEventLoopToFinish(uiHandler)

        verify(mockWorker).unlock()
        verifyNoMoreInteractions(mockWorker)
        verify(coreCompletionHandler).onError(expectedId, expectedException)
        verifyNoMoreInteractions(coreCompletionHandler)
        verifyNoInteractions(requestRepository)
    }

    private fun createRequestModel(ids: Array<String>): RequestModel {
        return CompositeRequestModel(
            "0",
            "https://emarsys.com",
            RequestMethod.POST,
            null,
            HashMap(),
            100,
            900000,
            ids
        )
    }

    private fun createResponseModel(requestModel: RequestModel): ResponseModel {
        return ResponseModel.Builder()
            .statusCode(200)
            .message("OK")
            .headers(HashMap())
            .body("{'key': 'value'}")
            .requestModel(requestModel)
            .build()
    }

    private fun createResponseModel(statusCode: Int): ResponseModel {
        val requestModel = mock<RequestModel>()
        whenever(requestModel.id).thenReturn(expectedId)
        return ResponseModel.Builder()
            .statusCode(statusCode)
            .body("body")
            .message("message")
            .requestModel(requestModel)
            .build()
    }

    private fun waitForEventLoopToFinish(handler: SdkHandler) {
        val latch = CountDownLatch(1)
        handler.post { latch.countDown() }
        latch.await()
    }

    private fun waitForEventLoopToFinish(handler: Handler) {
        val latch = CountDownLatch(1)
        handler.post { latch.countDown() }
        latch.await()
    }
}