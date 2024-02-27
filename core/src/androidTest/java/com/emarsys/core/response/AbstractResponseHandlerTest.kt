package com.emarsys.core.response

import com.emarsys.testUtil.AnnotationSpec
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AbstractResponseHandlerTest : AnnotationSpec() {
    private lateinit var abstractResponseHandler: AbstractResponseHandler


    @Before
    fun init() {
        abstractResponseHandler = mock()
    }

    @Test
    fun testProcessResponse_shouldCallHandleResponse_whenResponseShouldBeHandled() {
        whenever(abstractResponseHandler.shouldHandleResponse(any())).doReturn(true)

        val responseModel = ResponseModel.Builder()
            .statusCode(200)
            .message("OK")
            .requestModel(mock())
            .build()

        abstractResponseHandler.processResponse(responseModel)
        verify(abstractResponseHandler).handleResponse(responseModel)
    }

    @Test
    fun testProcessResponse_shouldNotCallHandleResponse_whenResponseShouldNotBeHandled() {
        whenever(abstractResponseHandler.shouldHandleResponse(any())).doReturn(false)

        val responseModel = ResponseModel.Builder()
            .statusCode(200)
            .message("OK")
            .requestModel(mock())
            .build()
        abstractResponseHandler.processResponse(responseModel)
        verify(abstractResponseHandler, times(0)).handleResponse(responseModel)
    }
}