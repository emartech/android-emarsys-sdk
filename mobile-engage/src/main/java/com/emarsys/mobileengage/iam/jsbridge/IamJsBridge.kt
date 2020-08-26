package com.emarsys.mobileengage.iam.jsbridge

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.emarsys.core.Mockable
import com.emarsys.core.util.JsonUtils.merge
import org.json.JSONException
import org.json.JSONObject


@RequiresApi(api = Build.VERSION_CODES.KITKAT)
@Mockable
class IamJsBridge(
        private val coreSdkHandler: Handler,
        private val uiHandler: Handler) {

    var webView: WebView? = null
    var onCloseListener: OnCloseListener? = null
    var onButtonClickedListener: OnButtonClickedListener? = null
    var onOpenExternalUrlListener: OnOpenExternalUrlListener? = null
    var onAppEventListener: OnAppEventListener? = null
    var onMEEventListener: OnMEEventListener? = null

    @JavascriptInterface
    fun close(jsonString: String) {
        uiHandler.post {
            onCloseListener?.invoke()
        }
    }

    @JavascriptInterface
    fun triggerAppEvent(jsonString: String) {
            handleJsBridgeEvent(jsonString, "name", uiHandler) { property, json ->
                onAppEventListener?.invoke(property, json)
                null
            }

    }

    @JavascriptInterface
    fun triggerMEEvent(jsonString: String) {
        handleJsBridgeEvent(jsonString, "name", coreSdkHandler) { property, json ->
            onMEEventListener?.invoke(property, json)
            null
        }
    }

    @JavascriptInterface
    fun buttonClicked(jsonString: String) {
        handleJsBridgeEvent(jsonString, "buttonId", coreSdkHandler) { property, json ->
            onButtonClickedListener?.invoke(property, json)
            null
        }
    }

    @JavascriptInterface
    fun openExternalLink(jsonString: String) {
        handleJsBridgeEvent(jsonString, "url", uiHandler) { property, json ->
            onOpenExternalUrlListener?.invoke(property, json)
            null
        }
    }

    private fun handleJsBridgeEvent(jsonString: String, property: String, handler: Handler, jsBridgeEventAction: (property: String?, json: JSONObject) -> JSONObject?) {
        try {
            val json = JSONObject(jsonString)
            val id = json.getString("id")
            if (json.has(property)) {
                val propertyValue = json.getString(property)
                handler.post {
                    try {
                        val resultPayload = jsBridgeEventAction(propertyValue, json)
                        sendSuccess(id, resultPayload)
                    } catch (e: Exception) {
                        sendError(id, e.message)
                    }
                }
            } else {
                sendError(id, String.format("Missing %s!", property))
            }
        } catch (ignored: JSONException) {
        }
    }

    fun sendSuccess(id: String?, resultPayload: JSONObject?) {
        try {
            val message = JSONObject()
                    .put("id", id)
                    .put("success", true)
            val result = merge(message, resultPayload)
            sendResult(result)
        } catch (ignore: JSONException) {
        }
    }

    fun sendError(id: String?, error: String?) {
        try {
            sendResult(JSONObject()
                    .put("id", id)
                    .put("success", false)
                    .put("error", error))
        } catch (ignore: JSONException) {
        }
    }

    fun sendResult(payload: JSONObject) {
        require(payload.has("id")) { "Payload must have an id!" }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            webView!!.evaluateJavascript(String.format("MEIAM.handleResponse(%s);", payload), null)
        } else {
            uiHandler.post { webView!!.evaluateJavascript(String.format("MEIAM.handleResponse(%s);", payload), null) }
        }
    }
}