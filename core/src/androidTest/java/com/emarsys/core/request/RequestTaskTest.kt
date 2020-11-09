package com.emarsys.core.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mapper
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseHandlersProcessor
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.IOException
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

class RequestTaskTest {
    private lateinit var requestModel: RequestModel
    private lateinit var coreCompletionHandler: CoreCompletionHandler
    private lateinit var connectionProvider: ConnectionProvider
    private lateinit var timestampProvider: TimestampProvider
    private lateinit var mockResponseHandlersProcessor: ResponseHandlersProcessor
    private lateinit var requestModelMappers: MutableList<Mapper<RequestModel, RequestModel>>

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
        requestModel = mock()
        coreCompletionHandler = mock()
        connectionProvider = ConnectionProvider()
        timestampProvider = mock()
        mockResponseHandlersProcessor = mock()
        requestModelMappers = ArrayList()
        whenever(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP_1, TIMESTAMP_2)
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
        val requestTask = RequestTask(requestModel, coreCompletionHandler, connectionProvider, timestampProvider, mockResponseHandlersProcessor, requestModelMappers)
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
        val requestTask = RequestTask(requestModel, coreCompletionHandler, connectionProvider, timestampProvider, mockResponseHandlersProcessor, requestModelMappers)
        requestTask.doInBackground()
        verify(mapper1).map(requestModel)
        verify(mapper2).map(expectedRequestModel1)
        verify(connectionProvider).provideConnection(expectedRequestModel2)
    }
}