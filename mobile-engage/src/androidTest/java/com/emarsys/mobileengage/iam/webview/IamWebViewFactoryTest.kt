package com.emarsys.mobileengage.iam.webview


import androidx.test.core.app.ActivityScenario
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactoryProvider
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ExtensionTestUtils.runOnMain
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch

class IamWebViewFactoryTest : AnnotationSpec() {

    private lateinit var mockJSCommandFactoryProvider: JSCommandFactoryProvider
    private lateinit var mockJSCommandFactory: JSCommandFactory
    private lateinit var mockJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider

    private lateinit var webViewFactory: IamWebViewFactory
    private lateinit var scenario: ActivityScenario<FakeActivity>

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

        scenario = ActivityScenario.launch(FakeActivity::class.java)
        scenario.onActivity { activity ->
            mockCurrentActivityProvider = mock {
                on { get() } doReturn activity
            }
            webViewFactory = IamWebViewFactory(
                mockJsBridgeFactory,
                mockJSCommandFactoryProvider,
                concurrentHandlerHolder,
                mockCurrentActivityProvider
            )
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testCreateWithNull() {
        val iamWebView = runOnMain {
            webViewFactory.create(null)
        }
        iamWebView::class.java shouldBe IamWebView::class.java
    }

    @Test
    fun testCreateWithActivity() {
        val scenario = ActivityScenario.launch(FakeActivity::class.java)
        val countDownLatch = CountDownLatch(1)
        scenario.onActivity { activity ->
            val iamWebView = runOnMain {
                webViewFactory.create(activity)
            }
            iamWebView::class.java shouldBe IamWebView::class.java
            countDownLatch.countDown()
        }
        countDownLatch.await()
        scenario.close()
    }
}