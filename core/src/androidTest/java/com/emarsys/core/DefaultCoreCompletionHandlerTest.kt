package com.emarsys.core

import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*

class DefaultCoreCompletionHandlerTest {

    companion object {
        const val STATUS_CODE = 500
        const val BODY = ""
        const val MESSAGE = "Internal server error"
        const val REQUEST_ID = "requestId"
    }

    private lateinit var abstractResponseHandler1: AbstractResponseHandler
    private lateinit var abstractResponseHandler2: AbstractResponseHandler
    private lateinit var handlers: List<AbstractResponseHandler>
    private lateinit var mockMap: MutableMap<String, CompletionListener>
    private lateinit var mockRequestModel: RequestModel
    private lateinit var coreCompletionHandler: DefaultCoreCompletionHandler
    private lateinit var responseErrorException: ResponseErrorException

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        abstractResponseHandler1 = mock(AbstractResponseHandler::class.java)
        abstractResponseHandler2 = mock(AbstractResponseHandler::class.java)
        handlers = listOf(abstractResponseHandler1, abstractResponseHandler2)
        mockMap = mock(MutableMap::class.java) as MutableMap<String, CompletionListener>
        mockRequestModel = createRequestModelMock(REQUEST_ID)
        coreCompletionHandler = DefaultCoreCompletionHandler(listOf(), mutableMapOf())
        responseErrorException = ResponseErrorException(429, "Some Errors", "body")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_handlersShouldNotBeNull() {
        DefaultCoreCompletionHandler(null, mapOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_completionListenerMapShouldNotBeNull() {
        DefaultCoreCompletionHandler(listOf(), null)
    }

    @Test
    fun testAddResponseHandlers() {
        val coreCompletionHandler = DefaultCoreCompletionHandler(mutableListOf(), mapOf())
        coreCompletionHandler.addResponseHandlers(handlers)

        coreCompletionHandler.responseHandlers shouldBe handlers
    }

    @Test
    fun testRegisterCompletionListener_addsListenerToMap() {
        val completionListenerMap = mutableMapOf<String, CompletionListener>()
        val coreCompletionHandler = DefaultCoreCompletionHandler(listOf(), completionListenerMap)
        val callback = mock(CompletionListener::class.java)
        coreCompletionHandler.register(mockRequestModel, callback)

        completionListenerMap[REQUEST_ID] shouldBe callback
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRegisterCompletionListener_requestModel_shouldNotBeNull() {
        coreCompletionHandler.register(null, mock(CompletionListener::class.java))
    }

    @Test
    fun testRegisterCompletionListener_withNullCompletionListener() {
        val coreCompletionHandler = DefaultCoreCompletionHandler(listOf(), mockMap)
        coreCompletionHandler.register(mockRequestModel, null)
        verifyZeroInteractions(mockMap)
    }

    @Test
    fun testOnSuccess_callsRegisteredCompletionListener() {
        val listener = mock(CompletionListener::class.java)
        coreCompletionHandler.register(mockRequestModel, listener)

        coreCompletionHandler.onSuccess(REQUEST_ID, null)

        verify(listener).onCompleted(null)
    }

    @Test
    fun testOnSuccess_should_call_nothing_whenNotRegistered() {
        val coreCompletionHandler = DefaultCoreCompletionHandler(listOf(), mockMap)

        coreCompletionHandler.onSuccess(REQUEST_ID, null)

        verify(mockMap)[REQUEST_ID]
        verifyNoMoreInteractions(mockMap)
    }

    @Test
    fun testOnSuccess_onlyCallsRegisteredCompletionListener_once_whenTheIdMatches() {
        val requestModel1 = createRequestModelMock("id1")
        val requestModel2 = createRequestModelMock("id2")

        val listener1 = mock(CompletionListener::class.java)
        val listener2 = mock(CompletionListener::class.java)

        coreCompletionHandler.register(requestModel1, listener1)
        coreCompletionHandler.register(requestModel2, listener2)

        coreCompletionHandler.onSuccess("id1", null)

        verify(listener1).onCompleted(null)
        verifyZeroInteractions(listener2)
    }

    @Test
    fun testOnSuccess_removesListener_afterCalled() {
        val listener = mock(CompletionListener::class.java)
        val spyMap = spy(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(listOf(), spyMap)

        coreCompletionHandler.onSuccess(REQUEST_ID, createAnyResponseModel())
        coreCompletionHandler.onSuccess(REQUEST_ID, createAnyResponseModel())

        verify(spyMap, times(2))[REQUEST_ID]
        verify(listener).onCompleted(null)
        verify(spyMap).remove(REQUEST_ID)
        verifyNoMoreInteractions(spyMap)
    }

    @Test
    fun testOnError_withException_callsRegisteredCompletionListener() {
        val listener = mock(CompletionListener::class.java)

        coreCompletionHandler.register(mockRequestModel, listener)

        coreCompletionHandler.onError(REQUEST_ID, responseErrorException)

        verify(listener).onCompleted(responseErrorException)
    }

    @Test
    fun testOnError_withException_should_not_callRegisteredCompletionListener_whenNotRegistered() {
        val coreCompletionListener = DefaultCoreCompletionHandler(listOf(), mockMap)

        coreCompletionListener.onError(REQUEST_ID, responseErrorException)
        verify(mockMap)[REQUEST_ID]
        verifyNoMoreInteractions(mockMap)
    }

    @Test
    fun testOnError_withException_removesListener_afterCalled() {
        val listener = mock(CompletionListener::class.java)
        val spyMap = spy(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(listOf(), spyMap)

        coreCompletionHandler.onError(REQUEST_ID, responseErrorException)
        coreCompletionHandler.onError(REQUEST_ID, responseErrorException)

        verify(spyMap, times(2))[REQUEST_ID]
        verify(listener).onCompleted(responseErrorException)
        verify(spyMap).remove(REQUEST_ID)
        verifyNoMoreInteractions(spyMap)
    }

    @Test
    fun testOnError_withResponseModel_callsRegisteredCompletionListener() {
        val listener = mock(CompletionListener::class.java)

        coreCompletionHandler.register(mockRequestModel, listener)

        val responseModel = createResponseModel(STATUS_CODE, MESSAGE, BODY)
        coreCompletionHandler.onError(REQUEST_ID, responseModel)

        verify(listener).onCompleted(responseModel.toError())
    }

    @Test
    fun testOnError_withResponseModel_should_not_callRegisteredCompletionListener_whenNotRegistered() {
        val coreCompletionListener = DefaultCoreCompletionHandler(listOf(), mockMap)
        coreCompletionListener.onError(REQUEST_ID, createResponseModel(500, "", ""))
        verify(mockMap)[REQUEST_ID]
        verifyNoMoreInteractions(mockMap)
    }

    @Test
    fun testOnError_withResponseModel_removesListener_afterCalled() {
        val listener = mock(CompletionListener::class.java)
        val spyMap = spy(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(listOf(), spyMap)

        val responseModel = createResponseModel(400, "", "")
        coreCompletionHandler.onError(REQUEST_ID, responseModel)
        coreCompletionHandler.onError(REQUEST_ID, responseModel)

        verify(spyMap, times(2))[REQUEST_ID]
        verify(listener).onCompleted(responseModel.toError())
        verify(spyMap).remove(REQUEST_ID)
        verifyNoMoreInteractions(spyMap)
    }

    @Test
    fun testMultipleRequestCompletions() {
        val listener1 = mock(CompletionListener::class.java)
        val listener2 = mock(CompletionListener::class.java)
        val listener3 = mock(CompletionListener::class.java)

        coreCompletionHandler.register(createRequestModelMock("id_1"), listener1)
        coreCompletionHandler.register(createRequestModelMock("id_2"), listener2)
        coreCompletionHandler.register(createRequestModelMock("id_3"), listener3)

        coreCompletionHandler.onSuccess("id_1", createResponseModel(200, "", ""))
        coreCompletionHandler.onSuccess("id_1", createResponseModel(200, "", ""))
        coreCompletionHandler.onError("id_2", createResponseModel(STATUS_CODE, MESSAGE, BODY))
        coreCompletionHandler.onError("id_2", createResponseModel(STATUS_CODE, MESSAGE, BODY))
        coreCompletionHandler.onError("id_3", ResponseErrorException(STATUS_CODE, MESSAGE, BODY))
        coreCompletionHandler.onError("id_3", ResponseErrorException(STATUS_CODE, MESSAGE, BODY))
        coreCompletionHandler.onSuccess("id_4", createResponseModel(200, "", ""))

        verify(listener1).onCompleted(null)
        verify(listener2).onCompleted(ResponseErrorException(STATUS_CODE, MESSAGE, BODY))
        verify(listener3).onCompleted(ResponseErrorException(STATUS_CODE, MESSAGE, BODY))
    }

    private fun createAnyResponseModel() = createResponseModel(200, "", "")

    private fun createResponseModel(statusCode: Int, message: String, body: String) =
            mock(ResponseModel::class.java).also {
                whenever(it.statusCode).thenReturn(statusCode)
                whenever(it.body).thenReturn(body)
                whenever(it.message).thenReturn(message)
            }

    private fun createRequestModelMock(requestId: String) =
            mock(RequestModel::class.java).also { whenever(it.id).thenReturn(requestId) }

    private fun ResponseModel.toError() = ResponseErrorException(statusCode, message, body)

}