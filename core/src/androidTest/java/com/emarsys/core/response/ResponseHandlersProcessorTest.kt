package com.emarsys.core.response

import io.kotest.matchers.collections.shouldContainAll
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock

class ResponseHandlersProcessorTest  {


    private lateinit var responseHandlersProcessor: ResponseHandlersProcessor

    @Before
    fun setUp() {
        responseHandlersProcessor = ResponseHandlersProcessor()
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

    @Test
    fun testAddReponseHandler() {
        val mockResponseHandler1 = mock(AbstractResponseHandler::class.java)
        val mockResponseHandler2 = mock(AbstractResponseHandler::class.java)

        val responseHandlers = mutableListOf(mockResponseHandler1)
        responseHandlersProcessor = ResponseHandlersProcessor(responseHandlers)

        responseHandlersProcessor.addResponseHandlers(listOf(mockResponseHandler2))

        responseHandlersProcessor.getResponseHandlers() shouldContainAll responseHandlers + mockResponseHandler2
    }

}