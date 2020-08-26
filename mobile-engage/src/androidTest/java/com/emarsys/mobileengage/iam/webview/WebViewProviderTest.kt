package com.emarsys.mobileengage.iam.webview

import android.webkit.WebView
import androidx.test.platform.app.InstrumentationRegistry
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test

class WebViewProviderTest {
    lateinit var webViewProvider: WebViewProvider

    @Before
    fun setUp() {
        webViewProvider = WebViewProvider(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun testProvideWebView() {
        val result = webViewProvider.provideWebView()
        result.javaClass shouldBe WebView::class.java
    }
}