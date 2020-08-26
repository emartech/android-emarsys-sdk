package com.emarsys.mobileengage.iam.jsbridge

import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import androidx.test.filters.SdkSuppress
import androidx.test.rule.ActivityTestRule
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.ThreadSpy
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch

@SdkSuppress(minSdkVersion = VERSION_CODES.KITKAT)
class IamJsBridgeTest {

    private lateinit var jsBridge: IamJsBridge
    private lateinit var webView: WebView
    private lateinit var coreSdkHandler: Handler
    private lateinit var eventHandler: EventHandler
    private lateinit var uiHandler: Handler

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun setUp() {
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        uiHandler = Handler(Looper.getMainLooper())
        jsBridge = IamJsBridge(
                coreSdkHandler,
                uiHandler
        )
        webView = mock()
        jsBridge.webView = webView
        eventHandler = mock()
    }

    @After
    fun tearDown() {
        coreSdkHandler.looper.quit()
    }

    @Test
    fun testClose_shouldInvokeOnCloseListener_OnClose() {
        var closed = false
        val latch = CountDownLatch(1)
        jsBridge.onCloseListener = {
            latch.countDown()
            closed = true
        }

        jsBridge.close("")
        latch.await()
        closed shouldBe true
    }

    @Test
    fun testClose_calledOnMainThread() {
        val mockOnCloseListener = mock<OnCloseListener>()
        jsBridge.onCloseListener = mockOnCloseListener
        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(mockOnCloseListener.invoke()).doAnswer(threadSpy)

        jsBridge.close("")

        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testButtonClicked_shouldInvokeOnButtonClickedListener_onButtonClicked() {
        val onButtonClickedListener: OnButtonClickedListener = mock()
        jsBridge.onButtonClickedListener = onButtonClickedListener

        val json = JSONObject(mapOf(
                "id" to "testId",
                "buttonId" to "testButtonId"))

        jsBridge.buttonClicked(json.toString())

        verify(onButtonClickedListener, timeout(1000)).invoke(eq("testButtonId"), any())
    }

    @Test
    fun testTriggerMEEvent_shouldInvokeOnMEEventListener_onTriggerMEEvent() {
        val onMEEventListener: OnMEEventListener = mock()
        jsBridge.onMEEventListener = onMEEventListener

        val json = JSONObject(mapOf(
                "id" to "testId",
                "buttonId" to "testButtonId",
                "name" to "testName"))

        jsBridge.triggerMEEvent(json.toString())

        verify(onMEEventListener, timeout(1000)).invoke(eq("testName"), any())
    }

    @Test
    fun testTriggerAppEvent_shouldNotThrowException_whenInAppMessageHandle_isNotSet() {
        val json = JSONObject().put("name", "eventName").put("id", "123456789")
        val jsBridge = IamJsBridge(
                coreSdkHandler,
                uiHandler
        )
        jsBridge.webView = webView

        jsBridge.triggerAppEvent(json.toString())
    }

    @Test
    fun testTriggerAppEvent_inAppMessageHandler_calledOnMainThread() {
        val json = JSONObject().put("name", "eventName").put("id", "123456789")
        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        val onAppEventListener = mock<OnAppEventListener>()
        whenever(onAppEventListener.invoke(any(), any())).doAnswer(threadSpy)
        val jsBridge = IamJsBridge(
                coreSdkHandler,
                uiHandler
        )
        jsBridge.webView = webView
        jsBridge.onAppEventListener = onAppEventListener
        jsBridge.triggerAppEvent(json.toString())
        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testTriggerAppEvent_shouldInvokeCallback_onSuccess() {
        val id = "123456789"
        val json = JSONObject().put("id", id).put("name", "value")

        jsBridge.triggerAppEvent(json.toString())

        val result = JSONObject().put("id", id).put("success", true)
        verify(webView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
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

        verify(webView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testButtonClicked_shouldCallAddOnRepository_onCoreSDKThread() {
        val mockButtonClickedListener: OnButtonClickedListener = mock()
        jsBridge.onButtonClickedListener = mockButtonClickedListener
        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(mockButtonClickedListener.invoke(any(), any())).doAnswer(threadSpy)
        val id = "12346789"
        val buttonId = "987654321"
        val json = JSONObject().put("id", id).put("buttonId", buttonId)
        jsBridge.buttonClicked(json.toString())
        threadSpy.verifyCalledOnCoreSdkThread()
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
        verify(webView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
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
        verify(webView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testOpenExternalLink_shouldInvokeCallback_whenActivityIsNull() {
        val id = "12346789"
        val json = JSONObject().put("id", id).put("url", "https://emarsys.com")

        jsBridge.onOpenExternalUrlListener = { _, _ ->
            throw java.lang.Exception("UI unavailable!")
        }
        jsBridge.openExternalLink(json.toString())
        val result = JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "UI unavailable!")
        verify(webView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testOpenExternalLink_shouldInvokeExternalUrlEventListener_handleEvent() {
        val id = "12346789"
        val url = "https://emarsys.com"
        val json = JSONObject().put("id", id).put("url", url)
        val mockOnOpenExternalUrlListener: OnOpenExternalUrlListener = mock()
        jsBridge.onOpenExternalUrlListener = mockOnOpenExternalUrlListener
        jsBridge.openExternalLink(json.toString())

        verify(mockOnOpenExternalUrlListener, timeout(1000)).invoke(eq(url), any())
    }

    @Test
    fun testOpenExternalLink_shouldBeCalled_onMainThread() {
        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        val mockOnOpenExternalUrlListener: OnOpenExternalUrlListener = mock()
        whenever(mockOnOpenExternalUrlListener.invoke(any(), any())).doAnswer(threadSpy)
        val id = "12346789"
        val url = "https://emarsys.com"
        val json = JSONObject().put("id", id).put("url", url)

        jsBridge.onOpenExternalUrlListener = mockOnOpenExternalUrlListener
        jsBridge.openExternalLink(json.toString())

        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testOpenExternalLink_shouldInvokeCallback_onSuccess() {
        val id = "12346789"
        val url = "https://emarsys.com"
        val json = JSONObject().put("id", id).put("url", url)
        val result = JSONObject()
                .put("id", id)
                .put("success", true)

        jsBridge.openExternalLink(json.toString())

        verify(webView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testOpenExternalLink_shouldInvokeCallback_whenUrlIsMissing() {
        val id = "12346789"
        val json = JSONObject().put("id", id)
        jsBridge.openExternalLink(json.toString())
        val result = JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Missing url!")
        verify(webView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test
    fun testOpenExternalLink_shouldInvokeCallback_whenIntentCannotBeResolved() {
        val id = "12346789"
        val json = JSONObject().put("id", id).put("url", "This is not a valid url!")
        jsBridge.onOpenExternalUrlListener = { _, _ ->
            throw Exception("Url cannot be handled by any application!")
        }
        jsBridge.openExternalLink(json.toString())
        val result = JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Url cannot be handled by any application!")
        verify(webView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSendResult_whenPayloadDoesNotContainId() {
        jsBridge.sendResult(JSONObject())
    }

    @Test
    fun testSendResult_shouldInvokeEvaluateJavascript_onWebView() {
        val json = JSONObject().put("id", "123456789").put("key", "value")
        jsBridge.sendResult(json)
        verify(webView, timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", json), null)
    }
}