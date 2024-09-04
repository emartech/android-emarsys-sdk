package com.emarsys.mobileengage.iam.webview


import android.app.Activity
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactoryProvider
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ExtensionTestUtils.runOnMain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class IamWebViewFactoryTest : AnnotationSpec() {

    private lateinit var mockJSCommandFactoryProvider: JSCommandFactoryProvider
    private lateinit var mockJSCommandFactory: JSCommandFactory
    private lateinit var mockJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    private lateinit var webViewFactory: IamWebViewFactory
    private lateinit var mockActivity: Activity

    @Before
    fun setUp() {
        mockJsBridge = mockk(relaxed = true)
        mockJsBridgeFactory = mockk {
            every { createJsBridge(any()) } returns mockJsBridge
        }

        mockJSCommandFactory = mockk()
        mockJSCommandFactoryProvider = mockk {
            every { provide() } returns mockJSCommandFactory
        }

        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()

        mockActivity = mockk(relaxed = true)
        webViewFactory = IamWebViewFactory(
            mockJsBridgeFactory,
            mockJSCommandFactoryProvider,
            concurrentHandlerHolder
        )
    }

    @Test
    fun testCreateWithNull() {
        val iamWebView = runOnMain {
            webViewFactory.create(mockActivity)
        }
        iamWebView::class.java shouldBe IamWebView::class.java
    }

    @Test
    fun testCreateWithActivity() {
        val iamWebView = runOnMain {
            webViewFactory.create(mockActivity)
        }
        iamWebView::class.java shouldBe IamWebView::class.java
    }
}