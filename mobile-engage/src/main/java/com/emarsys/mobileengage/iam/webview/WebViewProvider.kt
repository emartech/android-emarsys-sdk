package com.emarsys.mobileengage.iam.webview

import com.emarsys.core.Mockable

@Mockable
class WebViewProvider {
    fun provideEmarsysWebView(): EmarsysWebView {
        return EmarsysWebView()
    }
}