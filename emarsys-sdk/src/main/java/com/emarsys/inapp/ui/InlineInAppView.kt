package com.emarsys.inapp.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import com.emarsys.R
import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.getDependency
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.inline.InlineInAppWebViewFactory
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory
import com.emarsys.mobileengage.iam.jsbridge.OnAppEventListener
import com.emarsys.mobileengage.iam.jsbridge.OnCloseListener
import com.emarsys.mobileengage.iam.model.InAppMessage
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class InlineInAppView : LinearLayout {

    private lateinit var webView: WebView
    private var viewId: String? = null

    var onCloseListener: OnCloseListener? = null
    var onAppEventListener: OnAppEventListener? = null
    var onCompletionListener: CompletionListener? = null


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        commonConstructor(attrs)
    }

    constructor(context: Context) : super(context) {
        commonConstructor()
    }

    private fun commonConstructor(attrs: AttributeSet? = null) {
        visibility = GONE
        val intArray = IntArray(1).apply { this[0] = R.attr.view_id }
        val attributes = context.obtainStyledAttributes(attrs, intArray)
        viewId = attributes.getString(0)
        val webViewFactory: InlineInAppWebViewFactory = getDependency()
        webView = webViewFactory.create(MessageLoadedListener {
            visibility = View.VISIBLE
            onCompletionListener?.onCompleted(null)
        })

        addView(webView)
        with(webView.layoutParams) {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        if (viewId != null) {
            loadInApp(viewId!!)
        }

        attributes.recycle()
    }

    fun loadInApp(viewId: String) {
        this.viewId = viewId
        fetchInlineInAppMessage(viewId) { html ->
            if (html != null) {
                getDependency<Handler>("uiHandler").post {
                    webView.loadDataWithBaseURL(null, html, "text/html; charset=utf-8", "UTF-8", null)
                }
            } else {
                onCompletionListener?.onCompleted(IllegalArgumentException("Inline In-App HTML content must not be empty, please check your viewId!"))
            }
        }
    }

    private fun fetchInlineInAppMessage(viewId: String, callback: (String?) -> Unit) {
        val requestManager = getDependency<RequestManager>()
        val requestModelFactory = getDependency<MobileEngageRequestModelFactory>()
        val requestModel = requestModelFactory.createFetchInlineInAppMessagesRequest(viewId)

        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                val messageResponseModel = filterMessagesById(responseModel)
                if (messageResponseModel != null) {
                    val html = messageResponseModel.getString("html")

                    createJSBridge(messageResponseModel)
                    callback(html)
                } else {
                    callback(null)
                }
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                onCompletionListener?.onCompleted(ResponseErrorException(responseModel.statusCode, responseModel.message, responseModel.body))
            }

            override fun onError(id: String, cause: Exception) {
                onCompletionListener?.onCompleted(cause)
            }
        })
    }

    private fun filterMessagesById(responseModel: ResponseModel): JSONObject? {
        val inlineMessages: JSONArray? = responseModel.parsedBody?.optJSONArray("inlineMessages")
        if (inlineMessages != null) {
            for (i in 0 until inlineMessages.length()) {
                if (inlineMessages.getJSONObject(i).optString("viewId").toLowerCase(Locale.ENGLISH) == viewId?.toLowerCase(Locale.ENGLISH)) {
                    return inlineMessages.getJSONObject(i)
                }
            }
        }
        return null
    }

    private fun createJSBridge(messageResponseModel: JSONObject?) {
        val campaignId = messageResponseModel?.optString("campaignId")

        val instanceType = if (FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE)) {
            "defaultInstance"
        } else {
            "loggingInstance"
        }
        val inAppInternal: InAppInternal = getDependency(instanceType)
        val timestampProvider: TimestampProvider = getDependency()

        val buttonClickedRepository = getDependency<Repository<ButtonClicked, SqlSpecification>>("buttonClickedRepository")

        val jsCommandFactory = JSCommandFactory(getDependency(), Handler(Looper.getMainLooper()), getDependency(), inAppInternal,
                buttonClickedRepository, onCloseListener, onAppEventListener, timestampProvider)
        val jsBridgeFactory: IamJsBridgeFactory = getDependency()

        val jsBridge = jsBridgeFactory.createJsBridge(jsCommandFactory, InAppMessage(campaignId!!, null, null))
        webView.addJavascriptInterface(jsBridge, "Android")
        jsBridge.webView = webView
    }
}