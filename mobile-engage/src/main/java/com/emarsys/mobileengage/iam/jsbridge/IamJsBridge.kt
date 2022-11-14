package com.emarsys.mobileengage.iam.jsbridge

import android.os.Looper
import android.webkit.JavascriptInterface
import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.JsonUtils.merge
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory.CommandType
import com.emarsys.mobileengage.iam.model.InAppMessage
import com.emarsys.mobileengage.iam.webview.EmarsysWebView
import org.json.JSONException
import org.json.JSONObject


@Mockable
class IamJsBridge(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val jsCommandFactory: JSCommandFactory,
    private val inAppMessage: InAppMessage
) {

    var emarsysWebView: EmarsysWebView? = null

    @JavascriptInterface
    fun close(jsonString: String) {
        jsCommandFactory.create(CommandType.ON_CLOSE).invoke(null, JSONObject())
    }

    @JavascriptInterface
    fun triggerAppEvent(jsonString: String) {
        handleJsBridgeEvent(jsonString, "name") { property, json ->
            jsCommandFactory.create(CommandType.ON_APP_EVENT).invoke(property, json)
            null
        }
    }

    @JavascriptInterface
    fun triggerMEEvent(jsonString: String) {
        handleJsBridgeEvent(jsonString, "name") { property, json ->
            jsCommandFactory.create(CommandType.ON_ME_EVENT).invoke(property, json)
            null
        }
    }

    @JavascriptInterface
    fun buttonClicked(jsonString: String) {
        handleJsBridgeEvent(jsonString, "buttonId") { property, json ->
            jsCommandFactory.create(CommandType.ON_BUTTON_CLICKED, inAppMessage)
                .invoke(property, json)
            null
        }
    }

    @JavascriptInterface
    fun openExternalLink(jsonString: String) {
        handleJsBridgeEvent(jsonString, "url") { property, json ->
            val keepInAppOpen = json.optBoolean("keepInAppOpen", false)
            if (!keepInAppOpen) {
                jsCommandFactory.create(CommandType.ON_CLOSE).invoke(null, JSONObject())
            }
            jsCommandFactory.create(CommandType.ON_OPEN_EXTERNAL_URL).invoke(property, json)
            null
        }
    }

    private fun handleJsBridgeEvent(
        jsonString: String,
        property: String,
        jsBridgeEventAction: (property: String, json: JSONObject) -> JSONObject?
    ) {
        try {
            val json = JSONObject(jsonString)
            val id = json.getString("id")
            if (json.has(property)) {
                val propertyValue = json.getString(property)
                try {
                    val resultPayload = jsBridgeEventAction(propertyValue, json)
                    sendSuccess(id, resultPayload)
                } catch (e: Exception) {
                    sendError(id, e.message)
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
            sendResult(
                JSONObject()
                    .put("id", id)
                    .put("success", false)
                    .put("error", error)
            )
        } catch (ignore: JSONException) {
        }
    }

    fun sendResult(payload: JSONObject) {
        require(payload.has("id")) { "Payload must have an id!" }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            emarsysWebView!!.evaluateJavascript(payload)
        } else {
            concurrentHandlerHolder.postOnMain {
                emarsysWebView!!.evaluateJavascript(payload)
            }
        }
    }
}