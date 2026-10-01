package com.emarsys.predict.response

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.predict.DefaultPredictInternal

class VisitorIdResponseHandler(
    private val keyValueStore: KeyValueStore,
    private val predictServiceEndpointProvider: ServiceEndpointProvider
) : AbstractResponseHandler() {

    companion object {
        private const val CDV = "cdv"
    }

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        val isPredictUrl = responseModel.requestModel.url.toString()
            .startsWith(predictServiceEndpointProvider.provideEndpointHost())
        val hasVisitorIdCookie = responseModel.cookies["cdv"] != null
        return isPredictUrl && hasVisitorIdCookie
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val visitorId = responseModel.cookies[CDV]!!.value
        keyValueStore.putString(DefaultPredictInternal.VISITOR_ID_KEY, visitorId)
    }
}
