package com.emarsys.mobileengage.iam.webview

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import androidx.test.platform.app.InstrumentationRegistry
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class WebViewProviderTest {
    lateinit var webViewProvider: WebViewProvider

    @Before
    fun setUp() {
        webViewProvider = WebViewProvider(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun testProvideWebView() {
        val latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post {
            val result = webViewProvider.provideWebView()
            result?.javaClass shouldBe WebView::class.java
            latch.countDown()
        }
        latch.await()
    }
}