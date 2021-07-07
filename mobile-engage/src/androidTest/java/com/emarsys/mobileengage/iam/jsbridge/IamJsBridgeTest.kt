package com.emarsys.mobileengage.iam.jsbridge

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import androidx.test.rule.ActivityTestRule
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.iam.model.InAppMessage
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.fake.FakeActivity
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*


class IamJsBridgeTest {

    private val jsonObject = JSONObject(mapOf(
            "id" to "testId",
            "buttonId" to "testButtonId",
            "name" to "testName"))

    private lateinit var jsBridge: IamJsBridge
    private lateinit var mockWebView: WebView
    private lateinit var mockEventHandler: EventHandler
    private lateinit var uiHandler: Handler
    private lateinit var inAppMessage: InAppMessage
    private lateinit var mockJsCommandFactory: JSCommandFactory
    private lateinit var mockOnCloseListener: JSCommand
    private lateinit var mockOnAppEventListener: JSCommand
    private lateinit var mockOnButtonClickedListener: JSCommand
    private lateinit var mockOnOpenExternalUrlListener: JSCommand
    private lateinit var mockOnMEEventListener: JSCommand


    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun setUp() {
        inAppMessage = InAppMessage("campaignId", "sid", "url")
        uiHandler = Handler(Looper.getMainLooper())

        mockOnCloseListener = mock()
        mockOnAppEventListener = mock()
        mockOnButtonClickedListener = mock()
        mockOnOpenExternalUrlListener = mock()
        mockOnMEEventListener = mock()
        mockJsCommandFactory = mock() {
            on { create(JSCommandFactory.CommandType.ON_CLOSE) } doReturn (mockOnCloseListener)
            on { create(JSCommandFactory.CommandType.ON_ME_EVENT) } doReturn (mockOnMEEventListener)
            on { create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL) } doReturn (mockOnOpenExternalUrlListener)
            on { create(JSCommandFactory.CommandType.ON_APP_EVENT) } doReturn (mockOnAppEventListener)
            on { create(JSCommandFactory.CommandType.ON_BUTTON_CLICKED, inAppMessage) } doReturn (mockOnButtonClickedListener)
        }
        mockEventHandler = mock()
        mockWebView = mock()
        jsBridge = IamJsBridge(
                uiHandler,
                mockJsCommandFactory,
                inAppMessage
        )
        jsBridge.webView = mockWebView
    }

    @Test
    fun testClose_shouldInvokeOnCloseListener_createdByFactory() {
        jsBridge.close(jsonObject.toString())

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_CLOSE)
        verify(mockOnCloseListener).invoke(anyOrNull(), any())
    }

    @Test
    fun testOnAppEvent_shouldInvokeOnAppEventListener_createdByFactory() {
        jsBridge.triggerAppEvent(jsonObject.toString())

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_APP_EVENT)
        verify(mockOnAppEventListener).invoke(anyOrNull(), any())
    }

    @Test
    fun testOnButtonClickedEvent_shouldInvokeOnAppEventListener_createdByFactory() {
        jsBridge.buttonClicked(jsonObject.toString())

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_BUTTON_CLICKED, inAppMessage)
        verify(mockOnButtonClickedListener, timeout(2500)).invoke(anyOrNull(), any())
    }

    @Test
    fun testOnMEEvent_shouldInvokeOnAppEventListener_createdByFactory() {
        jsBridge.triggerMEEvent(jsonObject.toString())

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_ME_EVENT)
        verify(mockOnMEEventListener).invoke(anyOrNull(), any())
    }

    @Test
    fun testOnOpenExternalUrlEvent_shouldCreateOnCloseAndOpenExternalUrlCommands_then_InvokeOnExternalUrlListenerAndOnCloseListener() {
        val json = JSONObject(mapOf(
                "id" to "testId",
                "buttonId" to "testButtonId",
                "name" to "testName",
                "url" to "https://emarsys.com",
                "keepInAppOpen" to false))
        jsBridge.openExternalLink(json.toString())

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_CLOSE)
        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
        verify(mockOnCloseListener).invoke(isNull(), any())
        verify(mockOnOpenExternalUrlListener).invoke(anyOrNull(), any())
    }

    @Test
    fun testOnOpenExternalUrlEvent_shouldCreateOnCloseCommand_whenNokeepInAppOpenIsInJson() {
        val json = JSONObject(mapOf(
                "id" to "testId",
                "buttonId" to "testButtonId",
                "name" to "testName",
                "url" to "https://emarsys.com"))
        jsBridge.openExternalLink(json.toString())

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_CLOSE)
        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
        verify(mockOnCloseListener).invoke(isNull(), any())
        verify(mockOnOpenExternalUrlListener).invoke(anyOrNull(), any())
    }

    @Test
    fun testOnOpenExternalUrlEvent_shouldNotCreateAndInvokeCloseCommand_whenkeepInAppOpenIsTrueInJson() {
        val json = JSONObject(mapOf(
                "id" to "testId",
                "buttonId" to "testButtonId",
                "name" to "testName",
                "url" to "https://emarsys.com",
                "keepInAppOpen" to true))
        jsBridge.openExternalLink(json.toString())

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
        verifyNoMoreInteractions(mockJsCommandFactory)
        verifyZeroInteractions(mockOnCloseListener)
        verify(mockOnOpenExternalUrlListener).invoke(anyOrNull(), any())
    }

    @Test
    fun testTriggerAppEvent_shouldInvokeCallback_onSuccess() {
        val id = "123456789"
        val json = JSONObject().put("id", id).put("name", "value")

        jsBridge.triggerAppEvent(json.toString())

        val result = JSONObject().put("id", id).put("success", true)
        verify(mockWebView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testTriggerMeEvent_shouldInvokeCallback_onSuccess() {
        val id = "123456789"
        val json = JSONObject().put("id", id).put("name", "value")

        jsBridge.triggerMEEvent(json.toString())

        val result = JSONObject().put("id", id).put("success", true)
        verify(mockWebView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testButtonClicked_shouldInvokeCallback_onSuccess() {
        val id = "12346789"
        val buttonId = "987654321"
        val json = JSONObject().put("id", id).put("buttonId", buttonId)
        jsBridge.buttonClicked(json.toString())
        val result = JSONObject()
                .put("id", id)
                .put("success", true)
        verify(mockWebView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testOpenExternalLink_shouldInvokeCallback_onSuccess() {
        val id = "12346789"
        val url = "https://emarsys.com"
        val json = JSONObject().put("id", id).put("url", url).put("keepInAppOpen", false)
        jsBridge.openExternalLink(json.toString())
        val result = JSONObject()
                .put("id", id)
                .put("success", true)

        verify(mockWebView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testTriggerAppEvent_shouldInvokeCallback_whenNameIsMissing() {
        val id = "123456789"
        val json = JSONObject().put("id", id)
        val result = JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Missing name!")

        jsBridge.triggerAppEvent(json.toString())

        verify(mockWebView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testTriggerMeEvent_shouldInvokeCallback_whenNameIsMissing() {
        val id = "123456789"
        val json = JSONObject().put("id", id)
        val result = JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Missing name!")

        jsBridge.triggerMEEvent(json.toString())

        verify(mockWebView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testButtonClicked_shouldInvokeCallback_whenButtonIdIsMissing() {
        val id = "12346789"
        val json = JSONObject().put("id", id)
        jsBridge.buttonClicked(json.toString())
        val result = JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Missing buttonId!")
        verify(mockWebView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }


    @Test
    fun testOpenExternalLink_shouldInvokeCallback_whenUrlIsMissing() {
        val id = "12346789"
        val json = JSONObject().put("id", id).put("keepInAppOpen", false)
        jsBridge.openExternalLink(json.toString())
        val result = JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Missing url!")
        verify(mockWebView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSendResult_whenPayloadDoesNotContainId() {
        jsBridge.sendResult(JSONObject())
    }

    @Test
    fun testSendResult_shouldInvokeEvaluateJavascript_onWebView() {
        val json = JSONObject().put("id", "123456789").put("key", "value")
        jsBridge.sendResult(json)
        verify(mockWebView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", json), null)
    }
}