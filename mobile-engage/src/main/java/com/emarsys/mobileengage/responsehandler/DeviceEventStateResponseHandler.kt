package com.emarsys.mobileengage.responsehandler

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.util.RequestModelUtils

class DeviceEventStateResponseHandler(
        private val eventServiceProvider: ServiceEndpointProvider,
        private val eventServiceV4Provider: ServiceEndpointProvider,
        private val deviceEventStateStorage: StringStorage
) : AbstractResponseHandler() {

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        return FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) &&
                responseModel.statusCode in 200..299 &&
                RequestModelUtils.isCustomEvent(responseModel.requestModel, eventServiceProvider, eventServiceV4Provider) &&
                responseModel.parsedBody.has("deviceEventState")
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val deviceEventState = responseModel.parsedBody.getString("deviceEventState")
        deviceEventStateStorage.set(deviceEventState)
    }
}