package com.emarsys.mobileengage.iam.inline

import android.graphics.Color
import android.os.Build.VERSION_CODES.O
import androidx.test.filters.SdkSuppress
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.iam.webview.EmarsysWebView
import com.emarsys.mobileengage.iam.webview.IamWebViewClient
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.mobileengage.iam.webview.WebViewProvider
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.validateMockitoUsage
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch

class InlineInAppWebViewFactoryTest {
    private lateinit var mockEmarsysWebView: EmarsysWebView
    private lateinit var mockWebViewProvider: WebViewProvider
    private lateinit var inlineWebViewFactory: InlineInAppWebViewFactory
    private lateinit var mockMessageLoadedListener: MessageLoadedListener
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockEmarsysWebView = mock()

        mockWebViewProvider = mock {
            on { provideEmarsysWebView() }.doReturn(mockEmarsysWebView)
        }

        inlineWebViewFactory =
            InlineInAppWebViewFactory(mockWebViewProvider, concurrentHandlerHolder)
        mockMessageLoadedListener = mock()
    }

    @After
    fun tearDown() {
        validateMockitoUsage()
    }

    @Test
    fun testCreateShouldReturnEmarsysWebView() {
        val response = runOnUiThread { inlineWebViewFactory.create(mockMessageLoadedListener) }

        response shouldBe mockEmarsysWebView
    }

    @Test
    fun testCreateShouldReturnNull_whenWebViewCanNotBeCreated() {
        val response = runOnUiThread { inlineWebViewFactory.create(mockMessageLoadedListener) }

        response!!.webView shouldBe null
    }

    @Test
    fun testCreateShouldSetBackgroundTransparent() {
        runOnUiThread { inlineWebViewFactory.create(mockMessageLoadedListener) }

        verify(mockEmarsysWebView).setBackgroundColor(Color.TRANSPARENT)
    }

    @Test
    @SdkSuppress(minSdkVersion = O)
    fun testCreateShouldSetIamWebClient() {
        val testWebClient = IamWebViewClient(mockMessageLoadedListener, mock())
        whenever(mockEmarsysWebView.webViewClient).thenReturn(testWebClient)
        val emarsysWebView =
            runOnUiThread { inlineWebViewFactory.create(mockMessageLoadedListener) }
        var result: MessageLoadedListener? = null
        val webViewClient = emarsysWebView!!.webViewClient
        result = ReflectionTestUtils.getInstanceField(webViewClient!!, "listener")

        result!! shouldBeSameInstanceAs mockMessageLoadedListener
    }

    private fun <T> runOnUiThread(lambda: () -> T): T? {
        var result: T? = null
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.postOnMain {
            result = lambda.invoke()
            latch.countDown()
        }
        latch.await()
        return result
    }
}