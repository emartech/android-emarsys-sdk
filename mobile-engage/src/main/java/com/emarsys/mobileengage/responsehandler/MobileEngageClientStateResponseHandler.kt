package com.emarsys.mobileengage.responsehandler

import com.emarsys.core.Mockable
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.getCaseInsensitive
import com.emarsys.mobileengage.util.RequestModelUtils
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest

@Mockable
class MobileEngageClientStateResponseHandler(private val clientStateStorage: Storage<String?>) : AbstractResponseHandler() {

    companion object {
        private const val X_CLIENT_STATE = "X-Client-State"
    }

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        val hasClientState = getClientState(responseModel) != null

        return responseModel.isMobileEngageRequest() && hasClientState
    }

    override fun handleResponse(responseModel: ResponseModel) {
        clientStateStorage.set(getClientState(responseModel))
    }

    private fun getClientState(responseModel: ResponseModel): String? {
        return responseModel.headers.getCaseInsensitive(X_CLIENT_STATE)
    }
}
