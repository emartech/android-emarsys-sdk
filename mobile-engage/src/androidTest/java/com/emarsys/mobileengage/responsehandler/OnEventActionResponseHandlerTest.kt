package com.emarsys.mobileengage.responsehandler


import android.os.Looper
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.notification.ActionCommandFactory
import com.emarsys.mobileengage.notification.command.AppEventCommand
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.anyNotNull
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify

class OnEventActionResponseHandlerTest : AnnotationSpec() {

    private lateinit var responseHandler: OnEventActionResponseHandler
    private lateinit var mockActionCommandFactory: ActionCommandFactory
    private lateinit var mockAppEventCommand: AppEventCommand
    private lateinit var mockRepository: Repository<DisplayedIam, SqlSpecification>
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @Before
    fun setUp() {
        mockAppEventCommand = mock()
        mockActionCommandFactory = mock()
        mockRepository = mock()
        mockEventServiceInternal = mock()
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn 1L
        }
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        responseHandler = OnEventActionResponseHandler(
            mockActionCommandFactory,
            mockRepository,
            mockEventServiceInternal,
            mockTimestampProvider,
            concurrentHandlerHolder
        )
    }

    @After
    fun tearDown() {
        try {
            val looper: Looper? = concurrentHandlerHolder.coreLooper
            looper?.quitSafely()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testShouldHandlerResponse_whenResponseBodyIsEmpty() {
        val responseModel: ResponseModel = ResponseModel.Builder()
            .statusCode(200)
            .message("OK")
            .requestModel(
                RequestModel.Builder(TimestampProvider(), UUIDProvider())
                    .url("https://emarsys.com")
                    .build()
            )
            .build()

        responseHandler.shouldHandleResponse(responseModel) shouldBe false
    }

    @Test
    fun testShouldHandlerResponse_shouldReturnFalse_whenNoOnEventActionIsInTheResponseBody() {
        val responseModel: ResponseModel = createResponseModel("{}")

        responseHandler.shouldHandleResponse(responseModel) shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrue_whenResponseHasOnEventActionWithActions() {
        val responseModel = createResponseModel(
            """
            {
                "onEventAction": {
                    "campaignId": "1234",
                    "actions": []
                }
            }
        """.trimIndent()
        )

        responseHandler.shouldHandleResponse(responseModel) shouldBe true
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_whenResponseHasNoActions() {
        val responseModel = createResponseModel(
            """
            {
                "onEventAction": {
                    "campaignId": "123"
                }
            }
        """.trimIndent()
        )

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
        val responseModel = createTestResponseModel()

        responseHandler.handleResponse(responseModel)

        verify(mockActionCommandFactory).createActionCommand(captor.capture())

        captor.firstValue.toString() shouldBe JSONObject(appEventAction).toString()
    }

    @Test
    fun testHandleResponse_shouldRunActionCommand() {
        whenever(mockActionCommandFactory.createActionCommand(anyNotNull())).thenReturn(
            mockAppEventCommand
        )

        val responseModel = createTestResponseModel()

        responseHandler.handleResponse(responseModel)

        verify(mockAppEventCommand).run()
    }

    @Test
    fun testHandleResponse_shouldSaveDisplayedWithRepository() {
        val captor = argumentCaptor<DisplayedIam>()
        val iamToSave = DisplayedIam("1234", 1L)

        val responseModel = createTestResponseModel()

        responseHandler.handleResponse(responseModel)

        runBlocking {
            verify(mockRepository, timeout(1000)).add(captor.capture())
        }

        captor.firstValue.campaignId shouldBe iamToSave.campaignId
        captor.firstValue.timestamp shouldBe iamToSave.timestamp
    }

    @Test
    fun testHandleResponse_shouldSendDisplayedIamThroughEventServiceInternal() {
        val responseModel = createTestResponseModel()

        responseHandler.handleResponse(responseModel)

        verify(
            mockEventServiceInternal,
            timeout(1000)
        ).trackInternalCustomEventAsync(
            eq("inapp:viewed"),
            eq(mapOf("campaignId" to "1234")),
            anyOrNull()
        )
    }

    @Test
    fun testHandleResponse_shouldNotCrashWhenNoCampaignIdIsNotPresentInResponse() {
        val responseModel = createResponseModel(
            """
            {
                "onEventAction": {
                    "campaignId": "123"
                }
            }
        """.trimIndent()
        )

        responseHandler.handleResponse(responseModel)
    }

    private fun createResponseModel(body: String): ResponseModel {
        return ResponseModel.Builder()
            .statusCode(200)
            .message("OK")
            .body(body)
            .requestModel(
                RequestModel.Builder(TimestampProvider(), UUIDProvider())
                    .url("https://emarsys.com")
                    .build()
            )
            .build()
    }

    private fun createTestResponseModel(): ResponseModel {
        return ResponseModel.Builder()
            .statusCode(200)
            .message("OK")
            .body(
                """
                {
                   "onEventAction":{
                      "campaignId":"1234",
                      "actions":[
                         {
                            "type":"MEAppEvent",
                            "name":"nameOfTheAppEvent",
                            "payload":{
                               "key":"value",
                               "key2":"vale"
                            }
                         }
                      ]
                   }
                }
                """.trimIndent()
            )
            .requestModel(
                RequestModel.Builder(TimestampProvider(), UUIDProvider())
                    .url("https://emarsys.com")
                    .build()
            )
            .build()
    }
}