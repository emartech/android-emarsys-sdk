package com.emarsys.mobileengage.iam.inline

import android.graphics.Color
import android.os.Build.VERSION_CODES.O
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.core.Mockable
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.mobileengage.iam.webview.WebViewProvider
import com.emarsys.testUtil.ReflectionTestUtils
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.concurrent.CountDownLatch

class InlineInAppWebViewFactoryTest {
    private companion object {
        var html = """<!DOCTYPE html>
<html lang="en">
  <head>
    <script>
      window.onload = function() {
      };
        Android.onPageLoaded("{success:true}");
    </script>
  </head>
  <body style="background: transparent;">
  </body>
</html>"""
    }

    private lateinit var mockWebView: WebView
    private lateinit var mockWebViewProvider: WebViewProvider
    private lateinit var inlineWebViewFactory: InlineInAppWebViewFactory
    private lateinit var mockMessageLoadedListener: MessageLoadedListener
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        val setUpLatch = CountDownLatch(1)
        concurrentHandlerHolder.postOnMain {
            val webView = WebView(InstrumentationRegistry.getInstrumentation().targetContext)
            mockWebView = spy(webView)

            setUpLatch.countDown()
        }
        setUpLatch.await()

        mockWebViewProvider = mock {
            on { provideWebView() }.doReturn(mockWebView)
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
    fun testCreateShouldReturnWebView() {
        inlineWebViewFactory =
            InlineInAppWebViewFactory(mockWebViewProvider, concurrentHandlerHolder)
        val response = runOnUiThread { inlineWebViewFactory.create(mockMessageLoadedListener) }

        response shouldBe mockWebView
    }

    @Test
    fun testCreateShouldReturnNull_whenWebViewCanNotBeCreated() {
        whenever(mockWebViewProvider.provideWebView()).thenReturn(null)
        inlineWebViewFactory =
            InlineInAppWebViewFactory(mockWebViewProvider, concurrentHandlerHolder)
        val response = runOnUiThread { inlineWebViewFactory.create(mockMessageLoadedListener) }

        response shouldBe null
    }

    @Test
    fun testCreateShouldSetBackgroundTransparent() {
        runOnUiThread { inlineWebViewFactory.create(mockMessageLoadedListener) }

        verify(mockWebView).setBackgroundColor(Color.TRANSPARENT)
    }

    @Test
    @SdkSuppress(minSdkVersion = O)
    fun testCreateShouldSetIamWebClient() {
        val webView = runOnUiThread { inlineWebViewFactory.create(mockMessageLoadedListener) }
        var result: MessageLoadedListener? = null
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.postOnMain {
            val webViewClient = webView!!.webViewClient
            result = ReflectionTestUtils.getInstanceField(webViewClient, "listener")
            latch.countDown()
        }
        latch.await()

        result!! shouldBeSameInstanceAs mockMessageLoadedListener
    }

    @Mockable
    class TestJSInterface : IamJsBridge(
        mock(),
        mock(),
        mock()
    ) {
        @JavascriptInterface
        fun onPageLoaded(json: String?) {
        }
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