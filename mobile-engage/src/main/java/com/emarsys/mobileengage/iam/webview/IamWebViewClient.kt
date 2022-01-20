package com.emarsys.mobileengage.iam.webview

import android.webkit.WebView
import android.webkit.WebViewClient
import com.emarsys.core.handler.ConcurrentHandlerHolder
import kotlinx.coroutines.launch

class IamWebViewClient(
    private val listener: MessageLoadedListener,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder
) : WebViewClient() {
    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        concurrentHandlerHolder.uiScope.launch { listener.onMessageLoaded() }
    }
}