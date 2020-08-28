package com.emarsys.mobileengage.iam.webview

import android.content.Context
import android.webkit.WebView
import com.emarsys.core.Mockable

@Mockable
class WebViewProvider(private val context: Context) {
    fun provideWebView(): WebView {
        return WebView(context)
    }
}