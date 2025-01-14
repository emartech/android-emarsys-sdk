package com.emarsys.mobileengage.iam.jsbridge

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.webview.IamWebView
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.json.JSONObject

class IamJsBridgeTest : AnnotationSpec() {

    private val jsonObject = JSONObject(
        mapOf(
            "id" to "testId",
            "buttonId" to "testButtonId",
            "name" to "testName"
        )
    )

    private lateinit var jsBridge: IamJsBridge
    private lateinit var mockIamWebView: IamWebView
    private lateinit var mockEventHandler: EventHandler
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var inAppMetaData: InAppMetaData
    private lateinit var mockJsCommandFactory: JSCommandFactory
    private lateinit var mockOnCloseListener: JSCommand
    private lateinit var mockOnAppEventListener: JSCommand
    private lateinit var mockOnButtonClickedListener: JSCommand
    private lateinit var mockOnOpenExternalUrlListener: JSCommand
    private lateinit var mockCopyToClipboardListener: JSCommand
    private lateinit var mockOnMEEventListener: JSCommand

    @Before
    fun setUp() {
        inAppMetaData = InAppMetaData("campaignId", "sid", "url")
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockIamWebView = mockk(relaxed = true)
        mockOnCloseListener = mockk(relaxed = true)
        mockOnAppEventListener = mockk(relaxed = true)
        mockOnButtonClickedListener = mockk(relaxed = true)
        mockOnOpenExternalUrlListener = mockk(relaxed = true)
        mockOnMEEventListener = mockk(relaxed = true)
        mockCopyToClipboardListener = mockk(relaxed = true)
        mockJsCommandFactory = mockk {
            every { create(JSCommandFactory.CommandType.ON_CLOSE) } returns mockOnCloseListener
            every { create(JSCommandFactory.CommandType.ON_ME_EVENT) } returns mockOnMEEventListener
            every { create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL) } returns mockOnOpenExternalUrlListener
            every { create(JSCommandFactory.CommandType.ON_APP_EVENT) } returns mockOnAppEventListener
            every { create(JSCommandFactory.CommandType.ON_BUTTON_CLICKED) } returns mockOnButtonClickedListener
            every { create(JSCommandFactory.CommandType.ON_COPY_TO_CLIPBOARD) } returns mockCopyToClipboardListener
        }
        mockEventHandler = mockk(relaxed = true)
        jsBridge = IamJsBridge(
            concurrentHandlerHolder,
            mockJsCommandFactory
        )
        jsBridge.iamWebView = mockIamWebView
    }

    @Test
    fun testClose_shouldInvokeOnCloseListener_createdByFactory() {
        jsBridge.close(jsonObject.toString())

        verify {
            mockJsCommandFactory.create(JSCommandFactory.CommandType.ON_CLOSE)
            mockOnCloseListener.invoke(null, any())
        }
    }

    @Test
    fun testOnAppEvent_shouldInvokeOnAppEventListener_createdByFactory() {
        jsBridge.triggerAppEvent(jsonObject.toString())

        verify {
            mockJsCommandFactory.create(JSCommandFactory.CommandType.ON_APP_EVENT)
            mockOnAppEventListener.invoke("testName", any())
        }
    }

    @Test
    fun testOnButtonClickedEvent_shouldInvokeOnAppEventListener_createdByFactory() {
        jsBridge.buttonClicked(jsonObject.toString())

        verify(timeout = 2500) {
            mockJsCommandFactory.create(JSCommandFactory.CommandType.ON_BUTTON_CLICKED)
            mockOnButtonClickedListener.invoke("testButtonId", any())
        }
    }

    @Test
    fun testOnMEEvent_shouldInvokeOnAppEventListener_createdByFactory() {
        jsBridge.triggerMEEvent(jsonObject.toString())

        verify {
            mockJsCommandFactory.create(JSCommandFactory.CommandType.ON_ME_EVENT)
            mockOnMEEventListener.invoke("testName", any())
        }
    }

    @Test
    fun testOnOpenExternalUrlEvent_shouldCreateOnCloseAndOpenExternalUrlCommands_then_InvokeOnExternalUrlListenerAndOnCloseListener() {
        val json = JSONObject(
            mapOf(
                "id" to "testId",
                "buttonId" to "testButtonId",
                "name" to "testName",
                "url" to "https://emarsys.com",
                "keepInAppOpen" to false
            )
        )
        jsBridge.openExternalLink(json.toString())

        verify {
            mockJsCommandFactory.create(JSCommandFactory.CommandType.ON_CLOSE)
            mockJsCommandFactory.create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
            mockOnCloseListener.invoke(null, any())
            mockOnOpenExternalUrlListener.invoke("https://emarsys.com", any())
        }
    }

    @Test
    fun testOnOpenExternalUrlEvent_shouldCreateOnCloseCommand_whenNokeepInAppOpenIsInJson() {
        val json = JSONObject(
            mapOf(
                "id" to "testId",
                "buttonId" to "testButtonId",
                "name" to "testName",
                "url" to "https://emarsys.com"
            )
        )
        jsBridge.openExternalLink(json.toString())

        verify {
            mockJsCommandFactory.create(JSCommandFactory.CommandType.ON_CLOSE)
            mockJsCommandFactory.create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
            mockOnCloseListener.invoke(null, any())
            mockOnOpenExternalUrlListener.invoke("https://emarsys.com", any())
        }
    }

    @Test
    fun testOnOpenExternalUrlEvent_shouldNotCreateAndInvokeCloseCommand_whenkeepInAppOpenIsTrueInJson() {
        val json = JSONObject(
            mapOf(
                "id" to "testId",
                "buttonId" to "testButtonId",
                "name" to "testName",
                "url" to "https://emarsys.com",
                "keepInAppOpen" to true
            )
        )
        jsBridge.openExternalLink(json.toString())

        verify {
            mockJsCommandFactory.create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
        }
        verify { mockOnCloseListener wasNot Called }
        verify { mockOnOpenExternalUrlListener.invoke("https://emarsys.com", any()) }
    }

    @Test
    fun testTriggerAppEvent_shouldInvokeCallback_onSuccess() {
        val slot = slot<JSONObject>()
        val id = "123456789"
        val json = JSONObject().put("id", id).put("name", "value")

        jsBridge.triggerAppEvent(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe true
    }

    @Test
    fun testTriggerMeEvent_shouldInvokeCallback_onSuccess() {
        val slot = slot<JSONObject>()
        val id = "123456789"
        val json = JSONObject().put("id", id).put("name", "value")

        jsBridge.triggerMEEvent(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe true
    }

    @Test
    fun testButtonClicked_shouldInvokeCallback_onSuccess() {
        val slot = slot<JSONObject>()
        val id = "12346789"
        val buttonId = "987654321"
        val json = JSONObject().put("id", id).put("buttonId", buttonId)
        jsBridge.buttonClicked(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe true
    }

    @Test
    fun testOpenExternalLink_shouldInvokeCallback_onSuccess() {
        val slot = slot<JSONObject>()
        val id = "12346789"
        val url = "https://emarsys.com"
        val json = JSONObject().put("id", id).put("url", url).put("keepInAppOpen", false)
        jsBridge.openExternalLink(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe true
    }

    @Test
    fun testTriggerAppEvent_shouldInvokeCallback_whenNameIsMissing() {
        val slot = slot<JSONObject>()
        val id = "123456789"
        val json = JSONObject().put("id", id)
        jsBridge.triggerAppEvent(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe false
        slot.captured["error"] shouldBe "Missing name!"
    }

    @Test
    fun testTriggerMeEvent_shouldInvokeCallback_whenNameIsMissing() {
        val slot = slot<JSONObject>()
        val id = "123456789"
        val json = JSONObject().put("id", id)

        jsBridge.triggerMEEvent(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe false
        slot.captured["error"] shouldBe "Missing name!"
    }

    @Test
    fun testButtonClicked_shouldInvokeCallback_whenButtonIdIsMissing() {
        val slot = slot<JSONObject>()
        val id = "12346789"
        val json = JSONObject().put("id", id)
        jsBridge.buttonClicked(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe false
        slot.captured["error"] shouldBe "Missing buttonId!"
    }

    @Test
    fun testOpenExternalLink_shouldInvokeCallback_whenUrlIsMissing() {
        val slot = slot<JSONObject>()
        val id = "12346789"
        val json = JSONObject().put("id", id).put("keepInAppOpen", false)
        jsBridge.openExternalLink(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe false
        slot.captured["error"] shouldBe "Missing url!"
    }

    @Test
    fun testCopyToClipboard_shouldInvokeCallback_onSuccess() {
        val slot = slot<JSONObject>()
        val id = "12346789"
        val json = JSONObject().put("id", id).put("text", "testText")
        jsBridge.copyToClipboard(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe true
    }

    @Test
    fun testCopyToClipboard_shouldInvokeCallback_onError_whenTextIsMissing() {
        val slot = slot<JSONObject>()
        val id = "12346789"
        val json = JSONObject().put("id", id)
        jsBridge.copyToClipboard(json.toString())

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(capture(slot))
        }

        slot.captured["id"] shouldBe id
        slot.captured["success"] shouldBe false
    }

    @Test
    fun testSendResult_whenPayloadDoesNotContainId() {
        shouldThrow<IllegalArgumentException> {
            jsBridge.sendResult(JSONObject())
        }
    }

    @Test
    fun testSendResult_shouldInvokeEvaluateJavascript_onEmarsysWebviewWebView() {
        val json = JSONObject().put("id", "123456789").put("key", "value")
        jsBridge.sendResult(json)

        verify(timeout = 1000) {
            mockIamWebView.respondToJS(json)
        }
    }
}