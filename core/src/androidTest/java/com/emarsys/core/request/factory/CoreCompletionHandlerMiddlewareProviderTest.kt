package com.emarsys.core.request.factory

import android.os.Handler
import android.os.Looper
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.worker.CoreCompletionHandlerMiddleware
import com.emarsys.core.worker.Worker
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.should
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch

class CoreCompletionHandlerMiddlewareProviderTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var mockRequestRepository: Repository<RequestModel, SqlSpecification>
    private lateinit var mockUiHandler: Handler
    private lateinit var mockCoreSdkHandler: Handler
    private lateinit var mockCoreCompletionHandler: CoreCompletionHandler
    private lateinit var mockWorker: Worker
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockResponseModel: ResponseModel

    private lateinit var coreCompletionHandlerMiddlewareProvider: CoreCompletionHandlerMiddlewareProvider
    private lateinit var latch: CountDownLatch

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        latch = CountDownLatch(1)

        mockRequestModel = mock(RequestModel::class.java).also {
            whenever(it.id).thenReturn("requestId")
        }
        mockResponseModel = mock(ResponseModel::class.java).also {
            whenever(it.requestModel).thenReturn(mockRequestModel)
        }
        mockRequestRepository = (mock(Repository::class.java) as Repository<RequestModel, SqlSpecification>)
        mockUiHandler = Handler(Looper.getMainLooper())
        mockCoreSdkHandler = Handler(Looper.getMainLooper())
        mockCoreCompletionHandler = mock(CoreCompletionHandler::class.java).also {
            whenever(it.onSuccess(any(), any())).thenAnswer { latch.countDown() }
        }
        mockWorker = mock(Worker::class.java)

        coreCompletionHandlerMiddlewareProvider = CoreCompletionHandlerMiddlewareProvider(mockRequestRepository, mockUiHandler, mockCoreSdkHandler, mockCoreCompletionHandler)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestRepository_mustNotBeNull() {
        CoreCompletionHandlerMiddlewareProvider(
                null,
                mockUiHandler,
                mockCoreSdkHandler,
                mockCoreCompletionHandler
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_uiHandler_mustNotBeNull() {
        CoreCompletionHandlerMiddlewareProvider(
                mockRequestRepository,
                null,
                mockCoreSdkHandler,
                mockCoreCompletionHandler
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_coreSdkHandler_mustNotBeNull() {
        CoreCompletionHandlerMiddlewareProvider(
                mockRequestRepository,
                mockUiHandler,
                null,
                mockCoreCompletionHandler
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_coreCompletionHandler_mustNotBeNull() {
        CoreCompletionHandlerMiddlewareProvider(
                mockRequestRepository,
                mockUiHandler,
                mockCoreSdkHandler,
                null
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateCompletionHandler_worker_mustNotBeNull() {
        coreCompletionHandlerMiddlewareProvider.provideCompletionHandlerMiddleware(null)
    }

    @Test
    fun testCreateCompletionHandler_shouldReturnWithMiddleware_withDefaultCompletionHandler_whenWorkerIsPresent() {
        val result = coreCompletionHandlerMiddlewareProvider.provideCompletionHandlerMiddleware(mockWorker)

        result should beInstanceOf(CoreCompletionHandlerMiddleware::class)

        result.onSuccess("id", mockResponseModel)
        latch.await()

        verify(mockCoreCompletionHandler).onSuccess(eq("requestId"), any())
    }
}