package com.emarsys.core

import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*
import java.util.*

class DefaultCompletionHandlerTest {

    companion object {
        const val statusCode = 500
        const val body = ""
        const val message = "Internal server error"
    }

    private var abstractResponseHandler1: AbstractResponseHandler? = null
    private var abstractResponseHandler2: AbstractResponseHandler? = null
    private var handlers: List<AbstractResponseHandler>? = null

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        abstractResponseHandler1 = mock(AbstractResponseHandler::class.java)
        abstractResponseHandler2 = mock(AbstractResponseHandler::class.java)
        handlers = Arrays.asList<AbstractResponseHandler>(abstractResponseHandler1, abstractResponseHandler2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_handlersShouldNotBeNull() {
        DefaultCompletionHandler(null, mapOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_completionListenerMapShouldNotBeNull() {
        DefaultCompletionHandler(listOf(), null)
    }

    @Test
    fun testAddResponseHandlers() {
        val coreCompletionHandler = DefaultCompletionHandler(mutableListOf(), mapOf())
        coreCompletionHandler.addResponseHandlers(handlers)

        coreCompletionHandler.responseHandlers shouldBe handlers
    }

    @Test
    fun testRegisterCompletionListener_addsListenerToMap() {
        val completionListenerMap = mutableMapOf<String, CompletionListener>()
        val coreCompletionHandler = DefaultCompletionHandler(listOf(), completionListenerMap)
        val requestModel = mock(RequestModel::class.java)
        val callback = CompletionListener { }
        coreCompletionHandler.registerCompletionListener(requestModel, callback)

        completionListenerMap[requestModel.id] shouldBe callback
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRegisterCompletionListener_requestModel_shouldNotBeNull() {
        val coreCompletionHandler = DefaultCompletionHandler(listOf(), mutableMapOf())
        coreCompletionHandler.registerCompletionListener(null, mock(CompletionListener::class.java))
    }

    @Test
    fun testOnSuccess_callsRegisteredCompletionListener() {
        val coreCompletionHandler = DefaultCompletionHandler(listOf(), mutableMapOf())
        val requestModelId = "id"
        val requestModel = mock(RequestModel::class.java).also {
            whenever(it.id).thenReturn(requestModelId)
        }
        val listener = mock(CompletionListener::class.java)
        coreCompletionHandler.registerCompletionListener(requestModel, listener)

        coreCompletionHandler.onSuccess(requestModelId, null)

        verify(listener, times(1)).onCompleted(null)
    }

    @Test
    fun testOnSuccess_should_call_nothing_whenNotRegistered() {
        val mockMap = mock(MutableMap::class.java) as MutableMap<String, CompletionListener>

        val coreCompletionHandler = DefaultCompletionHandler(listOf(), mockMap)
        val requestModelId = "id"

        coreCompletionHandler.onSuccess(requestModelId, null)

        verify(mockMap, times(1))["id"]
    }

    @Test
    fun testOnSuccess_onlyCallsRegisteredCompletionListener_once_whenTheIdMatches() {
        val coreCompletionHandler = DefaultCompletionHandler(listOf(), mutableMapOf())

        val requestModel1 = createRequestModelMock("id1")
        val requestModel2 = createRequestModelMock("id2")

        val listener1 = mock(CompletionListener::class.java)
        val listener2 = mock(CompletionListener::class.java)

        coreCompletionHandler.registerCompletionListener(requestModel1, listener1)
        coreCompletionHandler.registerCompletionListener(requestModel2, listener2)

        coreCompletionHandler.onSuccess("id1", null)

        verify(listener1, times(1)).onCompleted(null)
        verify(listener2, times(0)).onCompleted(null)

    }

    @Test
    fun testOnError_withException_callsRegisteredCompletionListener() {
        val coreCompletionListener = DefaultCompletionHandler(listOf(), mutableMapOf())

        val requestModel = createRequestModelMock("id")
        val listener = mock(CompletionListener::class.java)

        coreCompletionListener.registerCompletionListener(requestModel, listener)

        val responseErrorException = ResponseErrorException(429, "Some Errors", "body")
        coreCompletionListener.onError(requestModel.id, responseErrorException)

        verify(listener, times(1)).onCompleted(ResponseErrorException(429, "Some Errors", "body"))
    }

    @Test
    fun testOnError_withException_should_not_callRegisteredCompletionListener_whenNotRegistered() {
        val mockMap = mock(MutableMap::class.java) as MutableMap<String, CompletionListener>

        val coreCompletionListener = DefaultCompletionHandler(listOf(), mockMap)
        val requestModel = createRequestModelMock("id")

        val responseErrorException = ResponseErrorException(429, "Some Errors", "body")
        coreCompletionListener.onError(requestModel.id, responseErrorException)
        verify(mockMap, times(1))["id"]
    }

    @Test
    fun testOnError_withResponseModel_callsRegisteredCompletionListener() {
        val coreCompletionListener = DefaultCompletionHandler(listOf(), mutableMapOf())

        val requestModel = createRequestModelMock("id")
        val listener = mock(CompletionListener::class.java)

        coreCompletionListener.registerCompletionListener(requestModel, listener)

        val responseModel = createResponseModel(statusCode, message, body)
        coreCompletionListener.onError(requestModel.id, responseModel)

        verify(listener, times(1)).onCompleted(ResponseErrorException(statusCode, message, body))
    }

    @Test
    fun testOnError_withResponseModel_should_not_callRegisteredCompletionListener_whenNotRegistered() {
        val coreCompletionListener = DefaultCompletionHandler(listOf(), mutableMapOf())
        val requestModel = createRequestModelMock("id")

        coreCompletionListener.onError(requestModel.id, createResponseModel(500, "", ""))
    }

    @Test
    fun testMultipleRequestCompletions() {
        val coreCompletionListener = DefaultCompletionHandler(listOf(), mutableMapOf())

        val listener1 = mock(CompletionListener::class.java)
        val listener2 = mock(CompletionListener::class.java)
        val listener3 = mock(CompletionListener::class.java)

        coreCompletionListener.registerCompletionListener(createRequestModelMock("id_1"), listener1)
        coreCompletionListener.registerCompletionListener(createRequestModelMock("id_2"), listener2)
        coreCompletionListener.registerCompletionListener(createRequestModelMock("id_3"), listener3)

        coreCompletionListener.onSuccess("id_1", createResponseModel(200, "", ""))
        coreCompletionListener.onError("id_2", createResponseModel(statusCode, message, body))
        coreCompletionListener.onError("id_3", ResponseErrorException(statusCode, message, body))
        coreCompletionListener.onSuccess("id_4", createResponseModel(200, "", ""))

        verify(listener1, times(1)).onCompleted(null)
        verify(listener2, times(1)).onCompleted(ResponseErrorException(statusCode, message, body))
        verify(listener3, times(1)).onCompleted(ResponseErrorException(statusCode, message, body))
    }

    private fun createResponseModel(statusCode: Int, message: String, body: String) =
            mock(ResponseModel::class.java).also {
                whenever(it.statusCode).thenReturn(statusCode)
                whenever(it.body).thenReturn(body)
                whenever(it.message).thenReturn(message)
            }

    private fun createRequestModelMock(requestId: String) =
            mock(RequestModel::class.java).also { whenever(it.id).thenReturn(requestId) }

}