package com.emarsys.mobileengage.iam.webview

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge

@Mockable
class IamStaticWebViewProvider(
    private val context: Context,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder
) {
    companion object {
        var webView: WebView? = null
    }

    fun loadMessageAsync(
        html: String,
        jsBridge: IamJsBridge,
        messageLoadedListener: MessageLoadedListener
    ) {
        concurrentHandlerHolder.postOnMain {
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
                it.webViewClient = IamWebViewClient(messageLoadedListener, concurrentHandlerHolder)
                it.setUiModeAutomatically()
                it.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        }
    }

    fun provideWebView(): WebView? {
        return webView
    }
}

fun WebView.setUiModeAutomatically() {
    if (AndroidVersionUtils.isBelowTiramisu) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    WebSettingsCompat.setForceDark(
                        settings,
                        WebSettingsCompat.FORCE_DARK_ON
                    )
                }
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    WebSettingsCompat.setForceDark(
                        settings,
                        WebSettingsCompat.FORCE_DARK_OFF
                    )
                }
            }
        }
    }
}