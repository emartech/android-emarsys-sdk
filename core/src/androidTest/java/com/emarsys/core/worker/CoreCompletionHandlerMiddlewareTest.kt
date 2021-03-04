package com.emarsys.core.worker

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.specification.FilterByRequestIds
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.shouldBe
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import java.util.concurrent.CountDownLatch

class CoreCompletionHandlerMiddlewareTest {
    private lateinit var coreCompletionHandler: CoreCompletionHandler
    private lateinit var requestRepository: Repository<RequestModel, SqlSpecification>
    private lateinit var worker: Worker
    private lateinit var expectedId: String
    private lateinit var middleware: CoreCompletionHandlerMiddleware
    private lateinit var captor: ArgumentCaptor<Message>
    private lateinit var uiHandler: Handler
    private lateinit var coreSdkHandler: CoreSdkHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setup() {
        expectedId = "expectedId"
        worker = mock()
        coreCompletionHandler = mock()
        requestRepository = mock()
        uiHandler = Handler(Looper.getMainLooper())
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        middleware = CoreCompletionHandlerMiddleware(worker, requestRepository, uiHandler, coreSdkHandler, coreCompletionHandler)
        captor = ArgumentCaptor.forClass(Message::class.java)
    }

    @Test
    fun testConstructor_handlerShouldNotBeNull() {
        Assert.assertNotNull(middleware.coreSDKHandler)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_workerShouldNotBeNull() {
        CoreCompletionHandlerMiddleware(null, requestRepository, uiHandler, coreSdkHandler, coreCompletionHandler)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_queueShouldNotBeNull() {
        CoreCompletionHandlerMiddleware(worker, null, uiHandler, coreSdkHandler, coreCompletionHandler)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_coreCompletionHandlerShouldNotBeNull() {
        CoreCompletionHandlerMiddleware(worker, requestRepository, uiHandler, coreSdkHandler, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_uiHandlerShouldNotBeNull() {
        CoreCompletionHandlerMiddleware(worker, requestRepository, null, coreSdkHandler, coreCompletionHandler)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_coreHandlerShouldNotBeNull() {
        CoreCompletionHandlerMiddleware(worker, requestRepository, uiHandler, null, coreCompletionHandler)
    }

    @Test
    fun testOnSuccess() {
        val captor = ArgumentCaptor.forClass(FilterByRequestIds::class.java)
        val expectedModel = createResponseModel(200)

        middleware.onSuccess(expectedId, expectedModel)

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        verify(worker).unlock()
        verify(worker).run()
        verifyNoMoreInteractions(worker)
        verify(requestRepository).remove(capture<FilterByRequestIds>(captor))
        val filter = captor.value

        filter.selectionArgs[0] shouldBe expectedModel.requestModel.id

        verify(coreCompletionHandler).onSuccess(expectedId, expectedModel)
    }

    @Test
    fun testOnSuccess_withCompositeModel_withManyRequestModels() {
        val ids = Array(2040) { i -> "id$i" }

        val responseModel = createResponseModel(createRequestModel(ids))

        middleware.onSuccess("0", responseModel)

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<FilterByRequestIds>().apply {
            verify(requestRepository, times(5)).remove(capture())
        }

    }

    @Test
    fun testOnSuccess_callsHandlerPost() {
        val handler: CoreSdkHandler = mock()

        middleware.coreSDKHandler = handler
        middleware.onSuccess(expectedId, createResponseModel(200))
        argumentCaptor<Runnable> {
            verify(handler).post(capture())
            val runnable = this.firstValue
            runnable.run()
        }

        verify(worker).run()
    }

    @Test
    fun testOnSuccess_withCompositeModel() {
        val ids = Array(2040) { i -> "id$i" }
        val responseModel = createResponseModel(createRequestModel(ids))

        middleware.onSuccess("0", responseModel)

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<String>().apply {
            verify(middleware.coreCompletionHandler, times(2040)).onSuccess(capture(), eq(responseModel))
            allValues shouldBe ids.toList()
        }
    }

    @Test
    fun testOnError_4xx() {
        val expectedModel = createResponseModel(403)

        middleware.onError(expectedId, expectedModel)

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<FilterByRequestIds>().apply {
            verify(requestRepository).remove(capture())
            firstValue.selectionArgs shouldBe arrayOf(expectedModel.requestModel.id)
        }

        verify(coreCompletionHandler).onError(expectedId, expectedModel)
        verify(worker).unlock()
        verify(worker).run()
        verifyNoMoreInteractions(worker)
    }

    @Test
    fun testOnError_4xx_callsHandlerPost() {
        val handler: CoreSdkHandler = mock()

        middleware.coreSDKHandler = handler

        middleware.onError(expectedId, createResponseModel(401))

        argumentCaptor<Runnable> {
            verify(handler).post(capture())
            val runnable = this.firstValue
            runnable.run()
        }

        verify(worker).run()
    }

    @Test
    fun testOnError_408_shouldHandleErrorAsRetriable() {
        val expectedModel = createResponseModel(408)

        middleware.onError(expectedId, expectedModel)

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        verify(worker).unlock()
        verifyNoMoreInteractions(worker)
        verifyZeroInteractions(coreCompletionHandler)
        verifyZeroInteractions(requestRepository)
    }

    @Test
    fun testOnError_429_shouldHandleErrorAsRetriable() {
        val expectedModel = createResponseModel(429)

        middleware.onError(expectedId, expectedModel)

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        verify(worker).unlock()
        verifyNoMoreInteractions(worker)
        verifyZeroInteractions(coreCompletionHandler)
        verifyZeroInteractions(requestRepository)
    }

    @Test
    fun testOnError_5xx() {
        val expectedModel = createResponseModel(500)

        middleware.onError(expectedId, expectedModel)

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        verify(worker).unlock()
        verifyNoMoreInteractions(worker)
        verifyZeroInteractions(coreCompletionHandler)
        verifyZeroInteractions(requestRepository)
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

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<String>().apply {
            verify(middleware.coreCompletionHandler, times(2040)).onError(capture(), eq(responseModel))
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

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<FilterByRequestIds>().apply {
            verify(requestRepository, times(4)).remove(capture())
            allValues.size shouldBeLessThanOrEqual 500
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

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        argumentCaptor<FilterByRequestIds>().apply {
            verify(requestRepository, times(5)).remove(capture())
            allValues.size shouldBeLessThanOrEqual 500
        }
    }

    @Test
    fun testOnError_withException() {
        val expectedException = Exception("Expected exception")

        middleware.onError(expectedId, expectedException)

        waitForEventLoopToFinish(coreSdkHandler)
        waitForEventLoopToFinish(uiHandler)

        verify(worker).unlock()
        verifyNoMoreInteractions(worker)
        verify(coreCompletionHandler).onError(expectedId, expectedException)
        verifyNoMoreInteractions(coreCompletionHandler)
        verifyZeroInteractions(requestRepository)
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
                ids)
    }

    private fun createResponseModel(requestModel: RequestModel): ResponseModel? {
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

    private fun waitForEventLoopToFinish(handler: CoreSdkHandler) {
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