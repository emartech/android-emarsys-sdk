package com.emarsys.inapp.ui

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.di.getDependency
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class InlineInAppView(private val viewId: String) {

    var html: String? = null
    var errorMessage: String? = null

    fun fetchInlineInAppMessage() {
        val requestManager = getDependency<RequestManager>()
        val requestModelFactory = getDependency<MobileEngageRequestModelFactory>()
        val requestModel = requestModelFactory.createFetchInlineInAppMessagesRequest(viewId)

        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                html = filterMessagesById(responseModel)?.getString("html")
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                errorMessage = responseModel.message
            }

            override fun onError(id: String, cause: Exception) {
                errorMessage = cause.message
            }
        })
    }

    private fun filterMessagesById(responseModel: ResponseModel): JSONObject? {
        val inlineMessages: JSONArray? = JSONObject(responseModel.body).optJSONArray("inlineMessages")
        if (inlineMessages != null) {
            for (i in 0 until inlineMessages.length()) {
                if (inlineMessages.getJSONObject(i).optString("viewId").toLowerCase(Locale.ENGLISH) == viewId.toLowerCase(Locale.ENGLISH)) {
                    return inlineMessages.getJSONObject(i)
                }
            }
        }
        return null
    }
}