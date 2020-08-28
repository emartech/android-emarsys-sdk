package com.emarsys.inapp.ui

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.getDependency
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.iam.inline.InlineInAppWebViewFactory
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.OnAppEventListener
import com.emarsys.mobileengage.iam.jsbridge.OnCloseListener
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class InlineInAppView : LinearLayout {

    private lateinit var webView: WebView
    private var viewId: String? = null
    private lateinit var jsBridge: IamJsBridge
    var onCloseListener: OnCloseListener? = null
        set(value) {
            jsBridge.onCloseListener = value
            field = value
        }

    var onAppEventListener: OnAppEventListener? = null
        set(value) {
            jsBridge.onAppEventListener = value
            field = value
        }

    var onCompletionListener: CompletionListener? = null


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        commonConstructor()
    }

    constructor(context: Context) : super(context) {
        commonConstructor()
    }

    private fun commonConstructor() {
        visibility = GONE

        val webViewFactory: InlineInAppWebViewFactory = getDependency()
        val jsBridgeFactory: IamJsBridgeFactory = getDependency()
        webView = webViewFactory.create(MessageLoadedListener {
            visibility = View.VISIBLE
        })

        jsBridge = jsBridgeFactory.createJsBridge()
        jsBridge.webView = webView
        webView.addJavascriptInterface(jsBridge, "Android")



        addView(webView)
        with(webView.layoutParams) {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    fun loadInApp(viewId: String) {


        this.viewId = viewId
        fetchInlineInAppMessage(viewId) {
            getDependency<Handler>("uiHandler").post {
                webView.loadDataWithBaseURL(null, it, "text/html; charset=utf-8", "UTF-8", null)
            }
        }
    }

    private fun fetchInlineInAppMessage(viewId: String, callback: (String?) -> Unit) {
        val requestManager = getDependency<RequestManager>()
        val requestModelFactory = getDependency<MobileEngageRequestModelFactory>()
        val requestModel = requestModelFactory.createFetchInlineInAppMessagesRequest(viewId)

        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                val html = filterMessagesById(responseModel)?.getString("html")
                callback(html)
                onCompletionListener?.onCompleted(null)

            }

            override fun onError(id: String, responseModel: ResponseModel) {
                callback(null)
                onCompletionListener?.onCompleted(ResponseErrorException(responseModel.statusCode, responseModel.message, responseModel.body))
            }

            override fun onError(id: String, cause: Exception) {
                callback(null)
                onCompletionListener?.onCompleted(cause)
            }
        })
    }

    private fun filterMessagesById(responseModel: ResponseModel): JSONObject? {
        val inlineMessages: JSONArray? = JSONObject(responseModel.body).optJSONArray("inlineMessages")
        if (inlineMessages != null) {
            for (i in 0 until inlineMessages.length()) {
                if (inlineMessages.getJSONObject(i).optString("viewId").toLowerCase(Locale.ENGLISH) == viewId?.toLowerCase(Locale.ENGLISH)) {
                    return inlineMessages.getJSONObject(i)
                }
            }
        }
        return null
    }
}