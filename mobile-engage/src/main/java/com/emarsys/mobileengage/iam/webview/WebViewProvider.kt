package com.emarsys.mobileengage.iam.webview

import android.content.Context
import android.webkit.WebView
import com.emarsys.core.Mockable
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog

@Mockable
class WebViewProvider(private val context: Context) {
    fun provideWebView(): WebView? {
        return try {
            WebView(context)
        } catch (e: Exception) {
            Logger.error(CrashLog(e))
            null
        }
    }
}