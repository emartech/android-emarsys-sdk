package com.emarsys.mobileengage.iam.webview


import android.app.Activity
import androidx.test.core.app.ActivityScenario
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory
import com.emarsys.mobileengage.iam.jsbridge.OnAppEventListener
import com.emarsys.mobileengage.iam.jsbridge.OnCloseListener
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ExtensionTestUtils.runOnMain
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class IamWebViewTest : AnnotationSpec() {

    private lateinit var iamWebView: IamWebView
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockJSBridgeFactory: IamJsBridgeFactory
    private lateinit var mockCommandFactory: JSCommandFactory
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var mockActivity: Activity
    private lateinit var scenario: ActivityScenario<FakeActivity>

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockActivity = mockk(relaxed = true)
        mockJsBridge = mockk(relaxed = true)
        val inAppMetaData = InAppMetaData("campaignId", "sid", "url")

        mockCommandFactory = mockk(relaxed = true)
            every { mockCommandFactory.inAppMetaData } returns inAppMetaData

        mockJSBridgeFactory = mockk(relaxed = true)
            every { mockJSBridgeFactory.createJsBridge(mockCommandFactory) } returns mockJsBridge

        scenario = ActivityScenario.launch(FakeActivity::class.java)
        scenario.onActivity { activity ->
            mockCurrentActivityProvider = mockk(relaxed = true)
                every { mockCurrentActivityProvider.get() } returns activity

            iamWebView = runOnMain {
                IamWebView(
                    concurrentHandlerHolder,
                    mockJSBridgeFactory,
                    mockCommandFactory,
                    mockCurrentActivityProvider.get()
                )
            }
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testInit_shouldSetWebView() {
        iamWebView.webView shouldNotBe null
        val jsEnabled = runOnMain {
            iamWebView.webView.settings.javaScriptEnabled
        }
        jsEnabled shouldBe true
    }

    @Test
    fun testLoad_shouldSetInAppMetaData_onCommandFactory() {
        val html = "<html>Hello World</html>"
        val campaignId = "test Id"
        val sid = "test sid"
        val url = "test url"
        val metaData = InAppMetaData(campaignId, sid, url)
        val messageLoadedListener: MessageLoadedListener = mockk(relaxed = true)

        runOnMain {
            iamWebView.load(html, metaData, messageLoadedListener)
        }

        verify { mockCommandFactory.inAppMetaData = metaData }
    }

    @Test
    fun testOnCloseTriggered() {
        val onCloseListener: OnCloseListener = mockk(relaxed = true)

        iamWebView.onCloseTriggered = onCloseListener

        verify { mockCommandFactory.onCloseTriggered = onCloseListener }
    }

    @Test
    fun testOnAppEventTriggered() {
        val onAppEventTriggered: OnAppEventListener = mockk(relaxed = true)

        iamWebView.onAppEventTriggered = onAppEventTriggered

        verify { mockCommandFactory.onAppEventTriggered = onAppEventTriggered }
    }

}