package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import org.json.JSONException

class InAppMessageResponseHandler(
    private val overlayInAppPresenter: OverlayInAppPresenter): AbstractResponseHandler() {

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        return responseModel.parsedBody?.let {
            try {
                val message = it.getJSONObject("message")
                message.has("html")
            } catch (ignored: JSONException) {
                false
            }
        } ?: false
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val responseBody = responseModel.parsedBody ?: return
        try {
            val message = responseBody.getJSONObject("message")
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