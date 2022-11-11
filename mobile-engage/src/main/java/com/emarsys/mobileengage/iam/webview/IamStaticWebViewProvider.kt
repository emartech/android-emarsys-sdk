package com.emarsys.mobileengage.iam.webview

import com.emarsys.core.Mockable

@Mockable
class IamStaticWebViewProvider {
    companion object {
        var emarsysWebView: EmarsysWebView? = null
    }

    fun provideWebView(): EmarsysWebView? {
        emarsysWebView = EmarsysWebView()
        return emarsysWebView
    }
}