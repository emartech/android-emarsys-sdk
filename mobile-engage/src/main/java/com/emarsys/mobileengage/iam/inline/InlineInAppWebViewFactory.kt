package com.emarsys.mobileengage.iam.inline

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.webview.IamWebViewClient
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.mobileengage.iam.webview.WebViewProvider
import java.util.concurrent.CountDownLatch

class InlineInAppWebViewFactory(private val webViewProvider: WebViewProvider, private val jsBridgeFactory: IamJsBridgeFactory) {

    fun create(messageLoadedListener: MessageLoadedListener): WebView {
        val webView: WebView = webViewProvider.provideWebView()

        val jsBridge = jsBridgeFactory.createJsBridge()

        val latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post {
            webView.settings.javaScriptEnabled = true
            webView.addJavascriptInterface(jsBridge, "Android")
            webView.setBackgroundColor(Color.TRANSPARENT)
            webView.webViewClient = IamWebViewClient(messageLoadedListener)
            latch.countDown()
        }
        latch.await()

        jsBridge.webView = webView
        return webView
    }
}