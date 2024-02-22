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
import com.emarsys.testUtil.ExtensionTestUtils.runOnMain
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class IamWebViewTest {

    private lateinit var iamWebView: IamWebView
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockJSBridgeFactory: IamJsBridgeFactory
    private lateinit var mockCommandFactory: JSCommandFactory
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var mockActivity: Activity
    private lateinit var scenario: ActivityScenario<FakeActivity>

    @BeforeEach
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockActivity = mock()
        mockJsBridge = mock()
        val inAppMetaData = InAppMetaData("campaignId", "sid", "url")

        mockCommandFactory = mock {
            whenever(it.inAppMetaData).thenReturn(inAppMetaData)
        }
        mockJSBridgeFactory = mock {
            whenever(it.createJsBridge(mockCommandFactory)).thenReturn(mockJsBridge)
        }

        scenario = ActivityScenario.launch(FakeActivity::class.java)
        scenario.onActivity { activity ->
            mockCurrentActivityProvider = mock {
                whenever(it.get()).thenReturn(activity)
            }
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

    @AfterEach
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
        val messageLoadedListener: MessageLoadedListener = mock()

        runOnMain {
            iamWebView.load(html, metaData, messageLoadedListener)
        }

        verify(mockCommandFactory).inAppMetaData = metaData
    }

    @Test
    fun testOnCloseTriggered() {
        val onCloseListener: OnCloseListener = mock()

        iamWebView.onCloseTriggered = onCloseListener

        verify(mockCommandFactory).onCloseTriggered = onCloseListener
    }

    @Test
    fun testOnAppEventTriggered() {
        val onAppEventTriggered: OnAppEventListener = mock()

        iamWebView.onAppEventTriggered = onAppEventTriggered

        verify(mockCommandFactory).onAppEventTriggered = onAppEventTriggered
    }

}