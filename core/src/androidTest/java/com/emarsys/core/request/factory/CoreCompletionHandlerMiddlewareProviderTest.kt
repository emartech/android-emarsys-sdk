package com.emarsys.core.request.factory

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.worker.CoreCompletionHandlerMiddleware
import com.emarsys.core.worker.Worker
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch

class CoreCompletionHandlerMiddlewareProviderTest  {

    private lateinit var mockRequestRepository: Repository<RequestModel, SqlSpecification>
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockCoreCompletionHandler: CoreCompletionHandler
    private lateinit var mockWorker: Worker
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var coreCompletionHandlerMiddlewareProvider: CoreCompletionHandlerMiddlewareProvider
    private lateinit var latch: CountDownLatch
    private lateinit var runnableFactory: RunnableFactory

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        latch = CountDownLatch(1)
        mockRequestModel = mock {
            whenever(it.id).thenReturn("requestId")
        }
        mockResponseModel = mock {
            whenever(it.requestModel).thenReturn(mockRequestModel)
        }
        mockRequestRepository = (mock())
        runnableFactory = DefaultRunnableFactory()

        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockCoreCompletionHandler = mock {
            whenever(it.onSuccess(any(), any())).thenAnswer { latch.countDown() }
        }
        mockWorker = mock()

        coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(
            mockRequestRepository,
            concurrentHandlerHolder
        )
    }

    @Test
    fun testCreateCompletionHandler_shouldReturnWithMiddleware_whenWorkerIsPresent() {
        val result = coreCompletionHandlerMiddlewareProvider.provideProxy(
            mockWorker,
            mockCoreCompletionHandler
        )

        result should beInstanceOf(CoreCompletionHandlerMiddleware::class)

        result.onSuccess("id", mockResponseModel)
        latch.await()

        verify(mockCoreCompletionHandler).onSuccess(eq("requestId"), any())
    }
}