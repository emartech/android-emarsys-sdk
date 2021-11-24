package com.emarsys.core

import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*

class DefaultCoreCompletionHandlerTest {

    companion object {
        const val STATUS_CODE = 500
        const val BODY = ""
        const val MESSAGE = "Internal server error"
        const val REQUEST_ID = "requestId"
    }

    private lateinit var mockMap: MutableMap<String, CompletionListener>
    private lateinit var mockRequestModel: RequestModel
    private lateinit var coreCompletionHandler: DefaultCoreCompletionHandler
    private lateinit var responseErrorException: ResponseErrorException

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        mockMap = mock()
        mockRequestModel = createRequestModelMock(REQUEST_ID)
        coreCompletionHandler = DefaultCoreCompletionHandler(mutableMapOf())
        responseErrorException = ResponseErrorException(429, "Some Errors", "body")
    }

    @Test
    fun testRegisterCompletionListener_addsListenerToMap() {
        val completionListenerMap = mutableMapOf<String, CompletionListener>()
        val coreCompletionHandler = DefaultCoreCompletionHandler(completionListenerMap)
        val callback: CompletionListener = mock()
        coreCompletionHandler.register(mockRequestModel, callback)

        completionListenerMap[REQUEST_ID] shouldBe callback
    }

    @Test
    fun testRegisterCompletionListener_withNullCompletionListener() {
        val coreCompletionHandler = DefaultCoreCompletionHandler(mockMap)
        coreCompletionHandler.register(mockRequestModel, null)
        verifyNoInteractions(mockMap)
    }

    @Test
    fun testOnSuccess_callsRegisteredCompletionListener() {
        val listener: CompletionListener = mock()
        coreCompletionHandler.register(mockRequestModel, listener)

        coreCompletionHandler.onSuccess(REQUEST_ID, mock())

        verify(listener).onCompleted(null)
    }

    @Test
    fun testOnSuccess_should_call_nothing_whenNotRegistered() {
        val coreCompletionHandler = DefaultCoreCompletionHandler(mockMap)

        coreCompletionHandler.onSuccess(REQUEST_ID, mock())

        verify(mockMap)[REQUEST_ID]
        verifyNoMoreInteractions(mockMap)
    }

    @Test
    fun testOnSuccess_onlyCallsRegisteredCompletionListener_once_whenTheIdMatches() {
        val requestModel1 = createRequestModelMock("id1")
        val requestModel2 = createRequestModelMock("id2")

        val listener1: CompletionListener = mock()
        val listener2: CompletionListener = mock()

        coreCompletionHandler.register(requestModel1, listener1)
        coreCompletionHandler.register(requestModel2, listener2)

        coreCompletionHandler.onSuccess("id1", mock())

        verify(listener1).onCompleted(null)
        verify(listener2, times(0))
    }

    @Test
    fun testOnSuccess_removesListener_afterCalled() {
        val listener: CompletionListener = mock()
        val spyMap = spy(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(spyMap)

        coreCompletionHandler.onSuccess(REQUEST_ID, createAnyResponseModel())
        coreCompletionHandler.onSuccess(REQUEST_ID, createAnyResponseModel())

        verify(spyMap, times(2))[REQUEST_ID]
        verify(listener).onCompleted(null)
        verify(spyMap).remove(REQUEST_ID)
        verifyNoMoreInteractions(spyMap)
    }

    @Test
    fun testOnError_withException_callsRegisteredCompletionListener() {
        val listener: CompletionListener = mock()

        coreCompletionHandler.register(mockRequestModel, listener)

        coreCompletionHandler.onError(REQUEST_ID, responseErrorException)

        verify(listener).onCompleted(responseErrorException)
    }

    @Test
    fun testOnError_withException_should_not_callRegisteredCompletionListener_whenNotRegistered() {
        val coreCompletionListener = DefaultCoreCompletionHandler(mockMap)

        coreCompletionListener.onError(REQUEST_ID, responseErrorException)
        verify(mockMap)[REQUEST_ID]
        verifyNoMoreInteractions(mockMap)
    }

    @Test
    fun testOnError_withException_removesListener_afterCalled() {
        val listener: CompletionListener = mock()
        val spyMap = spy(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(spyMap)

        coreCompletionHandler.onError(REQUEST_ID, responseErrorException)
        coreCompletionHandler.onError(REQUEST_ID, responseErrorException)

        verify(spyMap, times(2))[REQUEST_ID]
        verify(listener).onCompleted(responseErrorException)
        verify(spyMap).remove(REQUEST_ID)
        verifyNoMoreInteractions(spyMap)
    }

    @Test
    fun testOnError_withResponseModel_callsRegisteredCompletionListener() {
        val listener: CompletionListener = mock()

        coreCompletionHandler.register(mockRequestModel, listener)

        val responseModel = createResponseModel(STATUS_CODE, MESSAGE, BODY)
        coreCompletionHandler.onError(REQUEST_ID, responseModel)

        verify(listener).onCompleted(responseModel.toError())
    }

    @Test
    fun testOnError_withResponseModel_should_not_callRegisteredCompletionListener_whenNotRegistered() {
        val coreCompletionListener = DefaultCoreCompletionHandler(mockMap)
        coreCompletionListener.onError(REQUEST_ID, createResponseModel(500, "", ""))
        verify(mockMap)[REQUEST_ID]
        verifyNoMoreInteractions(mockMap)
    }

    @Test
    fun testOnError_withResponseModel_removesListener_afterCalled() {
        val listener: CompletionListener = mock()
        val spyMap = spy(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(spyMap)

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
        val listener1: CompletionListener = mock()
        val listener2: CompletionListener = mock()
        val listener3: CompletionListener = mock()

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
        mock<ResponseModel> {
            on { it.statusCode } doReturn statusCode
            on { it.body } doReturn body
            on { it.message } doReturn message
        }

    private fun createRequestModelMock(requestId: String) =
        mock<RequestModel> { on { id } doReturn requestId }

    private fun ResponseModel.toError() = ResponseErrorException(statusCode, message, body)

}