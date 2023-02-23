package com.emarsys.mobileengage.responsehandler

import android.content.ClipboardManager
import androidx.test.rule.ActivityTestRule
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
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.fake.FakeActivity
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*

class InAppMessageResponseHandlerTest {

    private lateinit var handler: InAppMessageResponseHandler
    private lateinit var presenter: OverlayInAppPresenter
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockDialog: IamDialog
    private lateinit var mockJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockClipboardManager: ClipboardManager
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun init() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockCurrentActivityProvider = mock {
            on { get() } doReturn activityRule.activity
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

    @Test
    fun testShouldHandleResponse_shouldReturnTrueWhenTheResponseHasHtmlAttribute() {
        val response = buildResponseModel("{'message': {'html':'some html'}}")
        Assert.assertTrue(handler.shouldHandleResponse(response))
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasANonJsonBody() {
        val response = buildResponseModel("Created")
        Assert.assertFalse(handler.shouldHandleResponse(response))
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoMessageAttribute() {
        val response = buildResponseModel("{'not_a_message': {'html':'some html'}}")
        Assert.assertFalse(handler.shouldHandleResponse(response))
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoHtmlAttribute() {
        val response = buildResponseModel("{'message': {'not_html':'some html'}}")
        Assert.assertFalse(handler.shouldHandleResponse(response))
    }

    @Test
    fun testHandleResponse_shouldCallPresentOnPresenter_withCorrectArguments() {
        val html = "<p>hello</p>"
        val responseBody = String.format("{'message': {'html':'%s', 'campaignId': '123'} }", html)
        val response = buildResponseModel(responseBody)
        handler.handleResponse(response)
        verify(presenter).present("123", null, null, response.requestModel.id, response.timestamp, html, null)
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