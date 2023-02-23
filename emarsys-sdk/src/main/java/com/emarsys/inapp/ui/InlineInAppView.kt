package com.emarsys.inapp.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.emarsys.R
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.iam.jsbridge.OnAppEventListener
import com.emarsys.mobileengage.iam.jsbridge.OnCloseListener
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.webview.IamWebView
import com.emarsys.mobileengage.iam.webview.IamWebViewCreationFailedException
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class InlineInAppView : LinearLayout {

    private var iamWebView: IamWebView? = null
    private var viewId: String? = null

    var onCloseListener: OnCloseListener? = null
        set(value) {
            iamWebView?.onCloseTriggered = value
            field = value
        }
    var onAppEventListener: OnAppEventListener? = null
        set(value) {
            iamWebView?.onAppEventTriggered = value
            field = value
        }
    var onCompletionListener: CompletionListener? = null

    private val webViewProvider = mobileEngage().webViewProvider
    private val concurrentHandlerHolder = mobileEngage().concurrentHandlerHolder
    private val requestManager = mobileEngage().requestManager
    private val requestModelFactory = mobileEngage().mobileEngageRequestModelFactory

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

        try {
            iamWebView = webViewProvider.provide()
        } catch (e: IamWebViewCreationFailedException) {
            onCompletionListener?.onCompleted(IllegalArgumentException("WebView can not be created, please try again later!"))
        }
        val iamWebView = iamWebView ?: return
        iamWebView.onAppEventTriggered = onAppEventListener
        iamWebView.onCloseTriggered = onCloseListener
        concurrentHandlerHolder.postOnMain {
            setupViewHierarchy(iamWebView)
            if (viewId != null) {
                loadInApp(viewId!!)
            }
        }
        attributes.recycle()
    }

    private fun setupViewHierarchy(iamWebView: IamWebView) {
        addView(iamWebView.webView)
        with(iamWebView.webView.layoutParams) {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    fun loadInApp(viewId: String) {
        concurrentHandlerHolder.coreHandler.post {
            this.viewId = viewId
            if (iamWebView == null) {
                onCompletionListener?.onCompleted(IllegalArgumentException("WebView can not be created, please try again later!"))
            } else {
                fetchInlineInAppMessage(viewId) { html, campaignId ->
                    concurrentHandlerHolder.postOnMain {
                        iamWebView!!.load(html, InAppMetaData(campaignId, null, null)) {
                            visibility = View.VISIBLE
                            onCompletionListener?.onCompleted(null)
                        }
                    }
                }
            }
        }
    }

    private fun fetchInlineInAppMessage(
        viewId: String,
        callback: (html: String, campaignId: String) -> Unit
    ) {
        val requestModel = requestModelFactory.createFetchInlineInAppMessagesRequest(viewId)
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                val messageResponseModel = filterMessagesById(responseModel)
                val html = messageResponseModel?.optString("html")
                val campaignId = messageResponseModel?.optString("campaignId")
                if (!html.isNullOrEmpty() && !campaignId.isNullOrEmpty()) {
                    callback(html, campaignId)
                } else {
                    onCompletionListener?.onCompleted(IllegalArgumentException("Inline In-App HTML content must not be empty, please check your viewId!"))
                }
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                onCompletionListener?.onCompleted(
                    ResponseErrorException(
                        responseModel.statusCode,
                        responseModel.message,
                        responseModel.body
                    )
                )
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
                if (inlineMessages.getJSONObject(i).optString("viewId")
                        .lowercase(Locale.ENGLISH) == viewId?.lowercase(Locale.ENGLISH)
                ) {
                    return inlineMessages.getJSONObject(i)
                }
            }
        }
        return null
    }

}