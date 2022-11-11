package com.emarsys.mobileengage.iam.inline

import android.graphics.Color
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.iam.webview.EmarsysWebView
import com.emarsys.mobileengage.iam.webview.IamWebViewClient
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.mobileengage.iam.webview.WebViewProvider

@Mockable
class InlineInAppWebViewFactory(
    private val webViewProvider: WebViewProvider,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder
) {

    fun create(messageLoadedListener: MessageLoadedListener): EmarsysWebView {
        val webView: EmarsysWebView = webViewProvider.provideEmarsysWebView()

        webView.enableJavaScript()
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.setUiMode()
        webView.webViewClient = IamWebViewClient(messageLoadedListener, concurrentHandlerHolder)

        return webView
    }
}