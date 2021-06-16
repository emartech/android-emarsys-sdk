package com.emarsys.mobileengage.iam.webview

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.webkit.WebView
import com.emarsys.core.Mockable
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge

@Mockable
class IamStaticWebViewProvider(private val context: Context, private val uiHandler: Handler) {
    companion object {
        var webView: WebView? = null
    }

    fun loadMessageAsync(html: String, jsBridge: IamJsBridge, messageLoadedListener: MessageLoadedListener?) {
        uiHandler.post {
            try {
                webView = WebView(context)
            } catch (e: Exception) {
                Logger.error(CrashLog(e))
            }
            jsBridge.webView = webView
            webView?.let {
                it.settings.javaScriptEnabled = true
                it.addJavascriptInterface(jsBridge, "Android")
                it.setBackgroundColor(Color.TRANSPARENT)
                it.webViewClient = IamWebViewClient(messageLoadedListener, uiHandler)
                it.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)

            }

        }
    }

    fun provideWebView(): WebView? {
        return webView
    }

}