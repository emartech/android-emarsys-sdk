package com.emarsys.mobileengage.iam.webview

import androidx.test.rule.ActivityTestRule
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactoryProvider
import com.emarsys.testUtil.ExtensionTestUtils.runOnMain
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.fake.FakeActivity
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class IamWebViewProviderTest {

    private lateinit var mockJSCommandFactoryProvider: JSCommandFactoryProvider
    private lateinit var mockJSCommandFactory: JSCommandFactory
    private lateinit var mockJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider

    private lateinit var webViewProvider: IamWebViewProvider

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun setUp() {
        mockJsBridge = mock()
        mockJsBridgeFactory = mock {
            on { createJsBridge(any()) } doReturn mockJsBridge
        }

        mockJSCommandFactory = mock()
        mockJSCommandFactoryProvider = mock {
            on { provide() } doReturn mockJSCommandFactory
        }

        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockCurrentActivityProvider = mock {
            on { get() } doReturn activityTestRule.activity
        }

        webViewProvider = IamWebViewProvider(mockJsBridgeFactory, mockJSCommandFactoryProvider, concurrentHandlerHolder, mockCurrentActivityProvider)
    }

    @Test
    fun testProvideWebView() {
        val iamWebView = runOnMain {
            webViewProvider.provide()
        }
        iamWebView::class.java shouldBe IamWebView::class.java
    }
}