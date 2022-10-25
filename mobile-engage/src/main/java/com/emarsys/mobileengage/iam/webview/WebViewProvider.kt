package com.emarsys.mobileengage.iam.webview

import android.content.Context
import android.webkit.WebView
import com.emarsys.core.Mockable
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.mobileengage.di.mobileEngage

@Mockable
class WebViewProvider {
    fun provideWebView(): WebView? {
        return try {
            WebView(mobileEngage().currentActivityProvider.get() as Context)
        } catch (e: Exception) {
            Logger.error(CrashLog(e))
            null
        }
    }
}