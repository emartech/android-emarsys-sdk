package com.emarsys.mobileengage.iam.webview

import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.fake.FakeMessageLoadedListener
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider.Companion.webView
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch

internal class TestJSInterface : IamJsBridge(mock(), mock(), mock()) {
    @JavascriptInterface
    fun onPageLoaded(json: String?) {
    }
}

class IamStaticWebViewProviderTest {
    companion object {
        private const val BASIC_HTML = "<html><head></head><body>webview content</body></html>"

        init {
            Mockito.mock(IamDialog::class.java)
            Mockito.mock(Handler::class.java)
        }
    }

    private lateinit var provider: IamStaticWebViewProvider
    private lateinit var listener: MessageLoadedListener
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var latch: CountDownLatch
    private lateinit var dummyJsBridge: IamJsBridge
    var html = String.format(
        """<!DOCTYPE html>
<html lang="en">
  <head>
    <script>
      window.onload = function() {
      };
        Android.%s("{success:true}");
    </script>
  </head>
  <body style="background: transparent;">
  </body>
</html>""", "onPageLoaded"
    )

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun init() {
        webView = null
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        provider =
            IamStaticWebViewProvider(getTargetContext().applicationContext, concurrentHandlerHolder)
        listener = Mockito.mock(MessageLoadedListener::class.java)
        latch = CountDownLatch(1)
        dummyJsBridge = TestJSInterface()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoadMessageAsync_shouldInvokeJsBridge_whenPageIsLoaded() {
        val jsInterface = Mockito.mock(TestJSInterface::class.java)
        provider.loadMessageAsync(html, jsInterface, FakeMessageLoadedListener(latch))
        latch.await()
        Mockito.verify(jsInterface).onPageLoaded("{success:true}")
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoadMessageAsync_shouldEventuallySetWebViewOnJSBridge() {
        val jsInterface = Mockito.mock(TestJSInterface::class.java)
        provider.loadMessageAsync(html, jsInterface, FakeMessageLoadedListener(latch))
        latch.await()
        Mockito.verify(jsInterface).webView = provider.provideWebView()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testProvideWebView_shouldReturnTheStaticInstance() {
        concurrentHandlerHolder.uiScope.launch {
            webView = WebView(getTargetContext())
            latch.countDown()
        }
        latch.await()
        Assert.assertEquals(webView, provider.provideWebView())
    }
}