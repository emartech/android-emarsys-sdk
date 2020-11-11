package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.notification.command.AppEventCommand
import com.emarsys.testUtil.mockito.anyNotNull
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class OnEventActionResponseHandlerTest {

    private lateinit var responseHandler: OnEventActionResponseHandler
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var mockAppEventCommand: AppEventCommand

    @Before
    fun setUp() {
        mockAppEventCommand = mock()
        mockActionCommandFactory = mock()
        responseHandler = OnEventActionResponseHandler(mockActionCommandFactory)
    }

    @Test
    fun testShouldHandlerResponse_whenResponseBodyIsEmpty() {
        val responseModel: ResponseModel = ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .requestModel(RequestModel.Builder(TimestampProvider(), UUIDProvider())
                        .url("https://emarsys.com")
                        .build())
                .build()

        responseHandler.shouldHandleResponse(responseModel) shouldBe false
    }

    @Test
    fun testShouldHandlerResponse_shouldReturnFalse_whenNoOnEventActionIsInTheResponseBody() {
        val responseModel: ResponseModel = createTestResponseModel("{}")

        responseHandler.shouldHandleResponse(responseModel) shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrue_whenResponseHasOnEventActionWithActions() {
        val responseModel = createTestResponseModel("""
            {
                "onEventAction": {
                    "actions": []
                }
            }
        """.trimIndent())

        responseHandler.shouldHandleResponse(responseModel) shouldBe true
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_whenResponseHasNoActions() {
        val responseModel = createTestResponseModel("""
            {
                "onEventAction": {
                    "campaignId": "123"
                }
            }
        """.trimIndent())

        responseHandler.shouldHandleResponse(responseModel) shouldBe false
    }

    @Test
    fun testHandleResponse_shouldCreateActionsWithFactory() {
        val captor = argumentCaptor<JSONObject>()
        val appEventAction = """
            {
                "type": "MEAppEvent",
                "name": "nameOfTheAppEvent",
                "payload": {"key":"value", "key2":"vale"}
            }
        """.trimIndent()
        val responseModel = createTestResponseModel("""
            {
                "onEventAction": {
                    "actions": [
                        $appEventAction            
                    ]
                }
            }
        """.trimIndent())

        responseHandler.handleResponse(responseModel)

        verify(mockActionCommandFactory).createActionCommand(captor.capture())

        captor.firstValue.toString() shouldBe JSONObject(appEventAction).toString()
    }

    @Test
    fun testHandleResponse_shouldRunActionCommand() {
        whenever(mockActionCommandFactory.createActionCommand(anyNotNull())).thenReturn(mockAppEventCommand)
        val appEventAction = """
            {
                "type": "MEAppEvent",
                "name": "nameOfTheAppEvent",
                "payload": {"key":"value", "key2":"vale"}
            }
        """.trimIndent()
        val responseModel = createTestResponseModel("""
            {
                "onEventAction": {
                    "actions": [
                        $appEventAction            
                    ]
                }
            }
        """.trimIndent())

        responseHandler.handleResponse(responseModel)

        verify(mockAppEventCommand).run()
    }

    private fun createTestResponseModel(body: String): ResponseModel {
        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(body)
                .requestModel(RequestModel.Builder(TimestampProvider(), UUIDProvider())
                        .url("https://emarsys.com")
                        .build())
                .build()
    }


}