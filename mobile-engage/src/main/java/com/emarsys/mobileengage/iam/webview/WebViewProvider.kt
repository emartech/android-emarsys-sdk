package com.emarsys.mobileengage.iam.webview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import com.emarsys.core.Mockable
import java.util.concurrent.CountDownLatch

@Mockable
class WebViewProvider(private val context: Context) {
    fun provideWebView(): WebView {
        var webView: WebView? = null
        val countDownLatch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post {
            webView = WebView(context)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        return webView!!
    }
}