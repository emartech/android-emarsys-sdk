package com.emarsys.mobileengage.responsehandler


import android.content.ClipboardManager
import androidx.test.core.app.ActivityScenario
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

class InAppMessageResponseHandlerTest : AnnotationSpec() {

    private lateinit var handler: InAppMessageResponseHandler
    private lateinit var presenter: OverlayInAppPresenter
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockDialog: IamDialog
    private lateinit var mockJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockClipboardManager: ClipboardManager
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    private lateinit var scenario: ActivityScenario<FakeActivity>

    @Before
    fun init() {
        scenario = ActivityScenario.launch(FakeActivity::class.java)
        scenario.onActivity { activity ->
            concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
            mockCurrentActivityProvider = mock {
                on { get() } doReturn activity
            }
            mockJsBridge = mock()
            mockClipboardManager = mock()
            mockJsBridgeFactory = mock {
                on { createJsBridge(any()) } doReturn mockJsBridge
            }
            mockDialog = mock()
            val dialogProvider = mock<IamDialogProvider> {
                on {
                    provideDialog(
                        any(),
                        anyOrNull(),
                        anyOrNull(),
                        any()
                    )
                } doReturn mockDialog
            }

            presenter = spy(
                OverlayInAppPresenter(
                    concurrentHandlerHolder,
                    dialogProvider,
                    mock(),
                    mockCurrentActivityProvider,
                )
            )
            handler = InAppMessageResponseHandler(presenter)
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrueWhenTheResponseHasHtmlAttribute() {
        val response = buildResponseModel("{'message': {'html':'some html'}}")
        handler.shouldHandleResponse(response) shouldBe true
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasANonJsonBody() {
        val response = buildResponseModel("Created")
        handler.shouldHandleResponse(response) shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoMessageAttribute() {
        val response = buildResponseModel("{'not_a_message': {'html':'some html'}}")
        handler.shouldHandleResponse(response) shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoHtmlAttribute() {
        val response = buildResponseModel("{'message': {'not_html':'some html'}}")
        handler.shouldHandleResponse(response) shouldBe false
    }

    @Test
    fun testHandleResponse_shouldCallPresentOnPresenter_withCorrectArguments() {
        val html = "<p>hello</p>"
        val responseBody = String.format("{'message': {'html':'%s', 'campaignId': '123'} }", html)
        val response = buildResponseModel(responseBody)
        handler.handleResponse(response)
        verify(presenter).present(
            "123",
            null,
            null,
            response.requestModel.id,
            response.timestamp,
            html,
            null
        )
    }

    private fun buildResponseModel(responseBody: String): ResponseModel {
        return ResponseModel.Builder()
            .statusCode(200)
            .message("OK")
            .body(responseBody)
            .requestModel(
                RequestModel.Builder(TimestampProvider(), UUIDProvider())
                    .url("https://emarsys.com")
                    .build()
            )
            .build()
    }
}