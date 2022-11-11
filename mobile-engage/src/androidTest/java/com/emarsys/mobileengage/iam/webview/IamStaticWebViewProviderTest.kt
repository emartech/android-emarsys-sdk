package com.emarsys.mobileengage.iam.webview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider.Companion.emarsysWebView
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
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
    private lateinit var latch: CountDownLatch
    private lateinit var context: Context

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun init() {
        emarsysWebView = null
        provider =
            IamStaticWebViewProvider()
        listener = Mockito.mock(MessageLoadedListener::class.java)
        latch = CountDownLatch(1)
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    @Throws(InterruptedException::class)
    fun testProvideWebView_shouldReturnTheStaticInstance() {
        val uiHandler = Handler(Looper.getMainLooper())
        var testWebView: EmarsysWebView? = null
        uiHandler.post {
            testWebView = provider.provideWebView()
            latch.countDown()
        }
        latch.await()
        Assert.assertEquals(testWebView, IamStaticWebViewProvider.emarsysWebView)
    }
}