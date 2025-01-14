package com.emarsys.core

import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify

class DefaultCoreCompletionHandlerTest : AnnotationSpec() {

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

    @Before
    fun init() {
        mockMap = mockk(relaxed = true)
        mockRequestModel = createRequestModelMock(REQUEST_ID)
        coreCompletionHandler = DefaultCoreCompletionHandler(mutableMapOf())
        responseErrorException = ResponseErrorException(429, "Some Errors", "body")
    }

    @Test
    fun testRegisterCompletionListener_addsListenerToMap() {
        val completionListenerMap = mutableMapOf<String, CompletionListener>()
        val coreCompletionHandler = DefaultCoreCompletionHandler(completionListenerMap)
        val callback: CompletionListener = mockk(relaxed = true)
        coreCompletionHandler.register(mockRequestModel, callback)

        completionListenerMap[REQUEST_ID] shouldBe callback
    }

    @Test
    fun testRegisterCompletionListener_withNullCompletionListener() {
        val coreCompletionHandler = DefaultCoreCompletionHandler(mockMap)

        coreCompletionHandler.register(mockRequestModel, null)

        confirmVerified(mockMap)
    }

    @Test
    fun testOnSuccess_callsRegisteredCompletionListener() {
        val listener: CompletionListener = mockk(relaxed = true)
        coreCompletionHandler.register(mockRequestModel, listener)

        coreCompletionHandler.onSuccess(REQUEST_ID, mockk(relaxed = true))

        verify { listener.onCompleted(null) }
    }

    @Test
    fun testOnSuccess_should_call_nothing_whenNotRegistered() {
        every { mockMap.get(any()) } returns null
        val coreCompletionHandler = DefaultCoreCompletionHandler(mockMap)

        coreCompletionHandler.onSuccess(REQUEST_ID, mockk(relaxed = true))

        verify { mockMap[REQUEST_ID] }
       confirmVerified(mockMap)
    }

    @Test
    fun testOnSuccess_onlyCallsRegisteredCompletionListener_once_whenTheIdMatches() {
        val requestModel1 = createRequestModelMock("id1")
        val requestModel2 = createRequestModelMock("id2")

        val listener1: CompletionListener = mockk(relaxed = true)
        val listener2: CompletionListener = mockk(relaxed = true)

        coreCompletionHandler.register(requestModel1, listener1)
        coreCompletionHandler.register(requestModel2, listener2)

        coreCompletionHandler.onSuccess("id1", mockk(relaxed = true))

        verify { listener1.onCompleted(null) }
        verify(exactly = 0) { listener2.onCompleted(any()) }
    }

    @Test
    fun testOnSuccess_removesListener_afterCalled() {
        val listener: CompletionListener = mockk(relaxed = true)
        val spyMap = spyk(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(spyMap)

        coreCompletionHandler.onSuccess(REQUEST_ID, createAnyResponseModel())
        coreCompletionHandler.onSuccess(REQUEST_ID, createAnyResponseModel())

        verify(exactly = 2) { spyMap[REQUEST_ID] }
        verify { listener.onCompleted(null) }
        verify { spyMap.remove(REQUEST_ID) }
        confirmVerified(spyMap)
    }

    @Test
    fun testOnError_withException_callsRegisteredCompletionListener() {
        val listener: CompletionListener = mockk(relaxed = true)

        coreCompletionHandler.register(mockRequestModel, listener)

        coreCompletionHandler.onError(REQUEST_ID, responseErrorException)

        verify { listener.onCompleted(responseErrorException) }
    }

    @Test
    fun testOnError_withException_should_not_callRegisteredCompletionListener_whenNotRegistered() {
        every { mockMap[REQUEST_ID] } returns null
        val coreCompletionListener = DefaultCoreCompletionHandler(mockMap)

        coreCompletionListener.onError(REQUEST_ID, responseErrorException)

        verify { mockMap[REQUEST_ID] }
        confirmVerified(mockMap)
    }

    @Test
    fun testOnError_withException_removesListener_afterCalled() {
        val listener: CompletionListener = mockk(relaxed = true)
        val spyMap = spyk(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(spyMap)

        coreCompletionHandler.onError(REQUEST_ID, responseErrorException)
        coreCompletionHandler.onError(REQUEST_ID, responseErrorException)

        verify(exactly = 2) { spyMap[REQUEST_ID] }
        verify { listener.onCompleted(responseErrorException) }
        verify { spyMap.remove(REQUEST_ID) }
        confirmVerified(spyMap)
    }

    @Test
    fun testOnError_withResponseModel_callsRegisteredCompletionListener() {
        val listener: CompletionListener = mockk(relaxed = true)
        val spyMap = spyk(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(spyMap)

        coreCompletionHandler.register(mockRequestModel, listener)

        val responseModel = createResponseModel(STATUS_CODE, MESSAGE, BODY)
        val expectedError = responseModel.toError()
        coreCompletionHandler.onError(REQUEST_ID, responseModel)

        verify { listener.onCompleted(expectedError) }
    }

    @Test
    fun testOnError_withResponseModel_should_not_callRegisteredCompletionListener_whenNotRegistered() {
        every { mockMap[REQUEST_ID] } returns null
        val coreCompletionListener = DefaultCoreCompletionHandler(mockMap)

        coreCompletionListener.onError(REQUEST_ID, createResponseModel(500, "", ""))

        verify { mockMap[REQUEST_ID] }
        confirmVerified(mockMap)
    }

    @Test
    fun testOnError_withResponseModel_removesListener_afterCalled() {
        val listener: CompletionListener = mockk(relaxed = true)
        val spyMap = spyk(HashMap<String, CompletionListener>().apply {
            put(REQUEST_ID, listener)
        })
        val coreCompletionHandler = DefaultCoreCompletionHandler(spyMap)

        val responseModel = createResponseModel(400, "", "")
        val expectedError = responseModel.toError()
        coreCompletionHandler.onError(REQUEST_ID, responseModel)
        coreCompletionHandler.onError(REQUEST_ID, responseModel)

        verify(exactly = 2) { spyMap[REQUEST_ID] }
        verify { listener.onCompleted(expectedError) }
        verify { spyMap.remove(REQUEST_ID) }
        confirmVerified(spyMap)
    }

    @Test
    fun testMultipleRequestCompletions() {
        val listener1: CompletionListener = mockk(relaxed = true)
        val listener2: CompletionListener = mockk(relaxed = true)
        val listener3: CompletionListener = mockk(relaxed = true)

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

        verify { listener1.onCompleted(null) }
        verify { listener2.onCompleted(ResponseErrorException(STATUS_CODE, MESSAGE, BODY)) }
        verify { listener3.onCompleted(ResponseErrorException(STATUS_CODE, MESSAGE, BODY)) }
    }

    private fun createAnyResponseModel() = createResponseModel(200, "", "")

    private fun createResponseModel(statusCode: Int, message: String, body: String): ResponseModel {
        val mockResponseModel = mockk<ResponseModel>(relaxed = true)
        every { mockResponseModel.statusCode } returns statusCode
        every { mockResponseModel.body } returns body
        every { mockResponseModel.message } returns message
        return mockResponseModel
    }

    private fun createRequestModelMock(requestId: String): RequestModel {
        val mockRequestModel = mockk<RequestModel>()
        every { mockRequestModel.id } returns requestId
        return mockRequestModel
    }

    private fun ResponseModel.toError() = ResponseErrorException(statusCode, message, body)

}