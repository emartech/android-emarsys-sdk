package com.emarsys.core.response

import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*

class AbstractResponseHandlerTest {
    private lateinit var abstractResponseHandler: AbstractResponseHandler

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

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