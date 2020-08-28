package com.emarsys.mobileengage.iam.inline

import android.graphics.Color
import android.webkit.WebView
import com.emarsys.core.Mockable
import com.emarsys.mobileengage.iam.webview.IamWebViewClient
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.mobileengage.iam.webview.WebViewProvider

@Mockable
class InlineInAppWebViewFactory(private val webViewProvider: WebViewProvider) {

    fun create(messageLoadedListener: MessageLoadedListener): WebView {
        val webView: WebView = webViewProvider.provideWebView()

        webView.settings.javaScriptEnabled = true
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.webViewClient = IamWebViewClient(messageLoadedListener)

        return webView
    }
}