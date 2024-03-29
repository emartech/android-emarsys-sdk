package com.emarsys.mobileengage.iam.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.mobileengage.iam.jsbridge.*
import com.emarsys.mobileengage.iam.model.InAppMetaData
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Mockable
class IamWebView(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    jsBridgeFactory: IamJsBridgeFactory,
    private val commandFactory: JSCommandFactory,
    activity: Context?
) {

    final var webView: WebView
    var onCloseTriggered: OnCloseListener? = null
        set(value) {
            commandFactory.onCloseTriggered = value
            field = value
        }
    var onAppEventTriggered: OnAppEventListener? = null
        set(value) {
            commandFactory.onAppEventTriggered = value
            field = value
        }

    final var jsBridge: IamJsBridge

    init {
        val context = activity ?: throw IamWebViewCreationFailedException()
        webView = try {
            WebView(context)
        } catch (ignored: Exception) {
            throw IamWebViewCreationFailedException()
        }

        jsBridge = jsBridgeFactory.createJsBridge(commandFactory)
        jsBridge.iamWebView = this

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(jsBridge, "Android")
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.setUiModeAutomatically()
    }

    fun load(
        html: String,
        inAppMetaData: InAppMetaData,
        messageLoadedListener: MessageLoadedListener
    ) {
        webView.webViewClient = IamWebViewClient(messageLoadedListener, concurrentHandlerHolder)
        commandFactory.inAppMetaData = inAppMetaData
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    fun respondToJS(payload: JSONObject) {
        webView.evaluateJavascript(String.format("MEIAM.handleResponse(%s);", payload), null)
    }

    fun purge() {
        jsBridge.iamWebView = null
        webView.removeJavascriptInterface("Android")
        webView.removeAllViews()
        webView.destroy()
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