package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.Mockable
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.util.RequestModelHelper
import org.json.JSONException
import org.json.JSONObject

@Mockable
class MobileEngageTokenResponseHandler(private val tokenKey: String,
                                       private val tokenStorage: Storage<String?>,
                                       private val requestModelHelper: RequestModelHelper) : AbstractResponseHandler() {

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        val body = responseModel.parsedBody
        val request = responseModel.requestModel

        return requestModelHelper.isMobileEngageRequest(request) && hasCorrectBody(body)
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val body = responseModel.parsedBody
        try {
            tokenStorage.set(body!!.getString(tokenKey))
        } catch (ignore: JSONException) {
        }

    }

    private fun hasCorrectBody(body: JSONObject?): Boolean {
        return body != null && body.has(tokenKey)
    }
}
