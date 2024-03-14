package com.emarsys.mobileengage.iam.jsbridge


import androidx.test.core.app.ActivityScenario
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.webview.IamWebView
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.capture
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions


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
    private lateinit var captor: ArgumentCaptor<JSONObject>
    private lateinit var scenario: ActivityScenario<FakeActivity>

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(FakeActivity::class.java)
        scenario.onActivity { activity ->
            inAppMetaData = InAppMetaData("campaignId", "sid", "url")
            concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
            mockIamWebView = mock()
            mockOnCloseListener = mock()
            mockOnAppEventListener = mock()
            mockOnButtonClickedListener = mock()
            mockOnOpenExternalUrlListener = mock()
            mockOnMEEventListener = mock()
            mockCopyToClipboardListener = mock()
            mockJsCommandFactory = mock {
                on { create(JSCommandFactory.CommandType.ON_CLOSE) } doReturn (mockOnCloseListener)
                on { create(JSCommandFactory.CommandType.ON_ME_EVENT) } doReturn (mockOnMEEventListener)
                on { create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL) } doReturn (mockOnOpenExternalUrlListener)
                on { create(JSCommandFactory.CommandType.ON_APP_EVENT) } doReturn (mockOnAppEventListener)
                on {
                    create(
                        JSCommandFactory.CommandType.ON_BUTTON_CLICKED
                    )
                } doReturn (mockOnButtonClickedListener)
                on { create(JSCommandFactory.CommandType.ON_COPY_TO_CLIPBOARD) } doReturn mockCopyToClipboardListener
            }
            mockEventHandler = mock()
            jsBridge = IamJsBridge(
                concurrentHandlerHolder,
                mockJsCommandFactory
            )
            jsBridge.iamWebView = mockIamWebView
            captor = ArgumentCaptor.forClass(JSONObject::class.java)
        }
    }

    @After
    fun tearDown() {
        scenario.close()
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

        verify(mockJsCommandFactory).create(
            JSCommandFactory.CommandType.ON_BUTTON_CLICKED
        )
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

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_CLOSE)
        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
        verify(mockOnCloseListener).invoke(isNull(), any())
        verify(mockOnOpenExternalUrlListener).invoke(anyOrNull(), any())
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

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_CLOSE)
        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
        verify(mockOnCloseListener).invoke(isNull(), any())
        verify(mockOnOpenExternalUrlListener).invoke(anyOrNull(), any())
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

        verify(mockJsCommandFactory).create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
        verifyNoMoreInteractions(mockJsCommandFactory)
        verifyNoInteractions(mockOnCloseListener)
        verify(mockOnOpenExternalUrlListener).invoke(anyOrNull(), any())
    }

    @Test
    fun testTriggerAppEvent_shouldInvokeCallback_onSuccess() {
        val id = "123456789"
        val json = JSONObject().put("id", id).put("name", "value")

        jsBridge.triggerAppEvent(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe true
    }

    @Test
    fun testTriggerMeEvent_shouldInvokeCallback_onSuccess() {
        val id = "123456789"
        val json = JSONObject().put("id", id).put("name", "value")

        jsBridge.triggerMEEvent(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe true
    }

    @Test
    fun testButtonClicked_shouldInvokeCallback_onSuccess() {
        val id = "12346789"
        val buttonId = "987654321"
        val json = JSONObject().put("id", id).put("buttonId", buttonId)
        jsBridge.buttonClicked(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe true
    }

    @Test
    fun testOpenExternalLink_shouldInvokeCallback_onSuccess() {
        val id = "12346789"
        val url = "https://emarsys.com"
        val json = JSONObject().put("id", id).put("url", url).put("keepInAppOpen", false)
        jsBridge.openExternalLink(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe true
    }

    @Test
    fun testTriggerAppEvent_shouldInvokeCallback_whenNameIsMissing() {
        val id = "123456789"
        val json = JSONObject().put("id", id)
        jsBridge.triggerAppEvent(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe false
        captor.value["error"] shouldBe "Missing name!"
    }

    @Test
    fun testTriggerMeEvent_shouldInvokeCallback_whenNameIsMissing() {
        val id = "123456789"
        val json = JSONObject().put("id", id)

        jsBridge.triggerMEEvent(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe false
        captor.value["error"] shouldBe "Missing name!"
    }

    @Test
    fun testButtonClicked_shouldInvokeCallback_whenButtonIdIsMissing() {
        val id = "12346789"
        val json = JSONObject().put("id", id)
        jsBridge.buttonClicked(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe false
        captor.value["error"] shouldBe "Missing buttonId!"
    }


    @Test
    fun testOpenExternalLink_shouldInvokeCallback_whenUrlIsMissing() {
        val id = "12346789"
        val json = JSONObject().put("id", id).put("keepInAppOpen", false)
        jsBridge.openExternalLink(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe false
        captor.value["error"] shouldBe "Missing url!"
    }

    @Test
    fun testCopyToClipboard_shouldInvokeCallback_onSuccess() {
        val id = "12346789"
        val json = JSONObject().put("id", id).put("text", "testText")
        jsBridge.copyToClipboard(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe true
    }

    @Test
    fun testCopyToClipboard_shouldInvokeCallback_onError_whenTextIsMissing() {
        val id = "12346789"
        val json = JSONObject().put("id", id)
        jsBridge.copyToClipboard(json.toString())

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(capture(captor))

        captor.value["id"] shouldBe id
        captor.value["success"] shouldBe false
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

        verify(
            mockIamWebView,
            timeout(1000)
        ).respondToJS(json)
    }
}