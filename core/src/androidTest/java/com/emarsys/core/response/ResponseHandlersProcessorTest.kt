package com.emarsys.core.response

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.matchers.collections.shouldContainAll
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock

class ResponseHandlersProcessorTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var responseHandlersProcessor: ResponseHandlersProcessor

    @Before
    fun setUp() {
        responseHandlersProcessor = ResponseHandlersProcessor()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_responseHandlers_mustNotBeNull() {
        ResponseHandlersProcessor(null)
    }

    @Test
    fun testRun() {
        val mockResponseHandler1 = mock(AbstractResponseHandler::class.java)
        val mockResponseHandler2 = mock(AbstractResponseHandler::class.java)

        val responseHandlers = listOf(mockResponseHandler1, mockResponseHandler2)

        val handler = ResponseHandlersProcessor(responseHandlers)
        val mockResponseModel = mock(ResponseModel::class.java)
        handler.process(mockResponseModel)
        inOrder(mockResponseHandler1, mockResponseHandler2).apply {
            verify(mockResponseHandler1).processResponse(mockResponseModel)
            verify(mockResponseHandler2).processResponse(mockResponseModel)
            verifyNoMoreInteractions()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAddReponseHandler_responseHandlers_mustNotBeNull() {
        responseHandlersProcessor.addResponseHandlers(null)
    }

    @Test
    fun testAddReponseHandler() {
        val mockResponseHandler1 = mock(AbstractResponseHandler::class.java)
        val mockResponseHandler2 = mock(AbstractResponseHandler::class.java)

        val responseHandlers = mutableListOf(mockResponseHandler1)
        responseHandlersProcessor = ResponseHandlersProcessor(responseHandlers)

        responseHandlersProcessor.addResponseHandlers(listOf(mockResponseHandler2))

        responseHandlersProcessor.responseHandlers shouldContainAll responseHandlers + mockResponseHandler2
    }

}