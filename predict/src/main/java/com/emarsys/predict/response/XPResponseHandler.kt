package com.emarsys.predict.response

import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.predict.DefaultPredictInternal
import com.emarsys.predict.endpoint.Endpoint

class XPResponseHandler(private val keyValueStore: KeyValueStore) : AbstractResponseHandler() {

    companion object {
        private const val XP = "xp"
    }

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        val isPredictUrl = responseModel.requestModel.url.toString().startsWith(Endpoint.PREDICT_BASE_URL)
        val hasXPCookie = responseModel.cookies["xp"] != null
        return isPredictUrl && hasXPCookie
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val xp = responseModel.cookies[XP]!!.value
        keyValueStore.putString(DefaultPredictInternal.XP_KEY, xp)
    }
}