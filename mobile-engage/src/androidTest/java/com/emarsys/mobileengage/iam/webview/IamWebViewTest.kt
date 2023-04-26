package com.emarsys.mobileengage.iam.webview

import android.app.Activity
import androidx.test.rule.ActivityTestRule
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
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
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

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockActivity = mock()
        mockJsBridge = mock()
        mockCommandFactory = mock {
            on { inAppMetaData } doReturn mock()
        }
        mockJSBridgeFactory = mock {
            on { createJsBridge(mockCommandFactory) } doReturn mockJsBridge
        }

        mockCurrentActivityProvider = mock {
            on { get() } doReturn activityRule.activity
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