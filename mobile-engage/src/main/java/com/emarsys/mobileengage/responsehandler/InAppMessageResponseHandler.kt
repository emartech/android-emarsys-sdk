package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import org.json.JSONException

class InAppMessageResponseHandler(
    private val overlayInAppPresenter: OverlayInAppPresenter): AbstractResponseHandler() {

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        val responseBody = responseModel.parsedBody
        val responseBodyNotNull = responseBody != null
        var shouldHandle = false
        if (responseBodyNotNull) {
            try {
                val message = responseBody!!.getJSONObject("message")
                shouldHandle = message.has("html")
            } catch (ignored: JSONException) {
            }
        }
        return shouldHandle
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val responseBody = responseModel.parsedBody
        try {
            val message = responseBody!!.getJSONObject("message")
            val html = message.getString("html")
            val campaignId = message.getString("campaignId")
            val requestId = responseModel.requestModel.id
            overlayInAppPresenter.present(
                campaignId,
                null,
                null,
                requestId,
                responseModel.timestamp,
                html,
                null
            )
        } catch (ignored: JSONException) {
        }
    }
}