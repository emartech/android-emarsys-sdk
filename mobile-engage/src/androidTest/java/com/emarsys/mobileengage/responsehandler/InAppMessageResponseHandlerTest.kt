package com.emarsys.mobileengage.responsehandler

import android.content.ClipboardManager
import com.emarsys.core.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class InAppMessageResponseHandlerTest  {

    private lateinit var handler: InAppMessageResponseHandler
    private lateinit var presenter: OverlayInAppPresenter
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockDialog: IamDialog
    private lateinit var mockJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockClipboardManager: ClipboardManager
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var mockCurrentActivityProvider: TransitionSafeCurrentActivityWatchdog

    @Before
    fun init() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockCurrentActivityProvider = mockk(relaxed = true)
        mockJsBridge = mockk(relaxed = true)
        mockClipboardManager = mockk(relaxed = true)
        mockJsBridgeFactory = mockk {
            every { createJsBridge(any()) } returns mockJsBridge
        }
        mockDialog = mockk(relaxed = true)

        val dialogProvider = mockk<IamDialogProvider> {
            every { provideDialog(any(), any(), any(), any()) } returns mockDialog
        }

        presenter = spyk(
            OverlayInAppPresenter(
                concurrentHandlerHolder,
                dialogProvider,
                mockk(relaxed = true),
                mockCurrentActivityProvider,
            )
        )
        handler = InAppMessageResponseHandler(presenter)
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

        verify {
            presenter.present(
                "123",
                null,
                null,
                response.requestModel.id,
                response.timestamp,
                html,
                null
            )
        }
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