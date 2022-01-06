package com.emarsys.core.request

import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class RequestTaskTest {
    private lateinit var connectionProvider: ConnectionProvider
    private lateinit var mockTimestampProvider: TimestampProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    companion object {
        private const val WRONG_URL = "https://localhost/missing"
        private const val TIMESTAMP_1: Long = 600
        private const val TIMESTAMP_2: Long = 1600
    }

    @Before
    fun setUp() {
        connectionProvider = ConnectionProvider()
        mockTimestampProvider = mock()
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(TIMESTAMP_1, TIMESTAMP_2)
    }

    @Test
    fun testExecute_shouldBeResilientToRuntimeExceptions() {
        val requestModel: RequestModel = mock {
            on { url } doReturn URL(WRONG_URL)
        }
        val runtimeException: Exception = RuntimeException("Sneaky exception")
        val connection: HttpsURLConnection = mock {
            on { connect() } doThrow runtimeException
        }
        connectionProvider = mock {
            on { it.provideConnection(requestModel) } doReturn connection
        }

        val requestTask = createRequestTask(requestModel)

        try {
            requestTask.doInBackground()
        } catch (e: Exception) {
            Assert.fail("Request Task should handle exception: " + e.message)
        }
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