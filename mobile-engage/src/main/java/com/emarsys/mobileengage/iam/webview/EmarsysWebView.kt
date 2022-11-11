package com.emarsys.mobileengage.iam.webview

import android.content.Context
import android.content.res.Configuration
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.emarsys.core.Mockable
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import org.json.JSONObject

@Mockable
class EmarsysWebView {
    val webView: WebView? = try {
        WebView(mobileEngage().currentActivityProvider.get() as Context)
    } catch (e: Exception) {
        e.printStackTrace()
        Logger.error(CrashLog(e))
        null
    }

    var webViewClient: WebViewClient? = null
        set(value) {
            webView?.webViewClient = value!!
            field = value
        }
        get() = field

    fun enableJavaScript() {
        webView?.settings?.javaScriptEnabled = true
    }

    fun setBackgroundColor(value: Int) {
        webView?.setBackgroundColor(value)
    }

    fun setUiMode() {
        webView?.setUiModeAutomatically()
    }

    fun addJavascriptInterface(jsBridge: IamJsBridge, name: String) {
        webView?.addJavascriptInterface(jsBridge, name)
    }

    fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String,
        encoding: String,
        historyUrl: String?
    ) {
        webView?.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    fun evaluateJavascript(script: JSONObject) {
        webView?.evaluateJavascript(String.format("MEIAM.handleResponse(%s);", script), null)
    }

    fun purge() {
        webView?.removeAllViews()
        webView?.destroy()
    }

    fun isNull(): Boolean {
        return this.webView == null
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