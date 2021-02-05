package com.emarsys.mobileengage.responsehandler

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.util.RequestModelUtils
import com.emarsys.mobileengage.util.RequestModelUtils.isCustomEvent

class DeviceEventStateResponseHandler(
        private val deviceEventStateStorage: StringStorage
) : AbstractResponseHandler() {

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        return FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) &&
                responseModel.statusCode in 200..299 &&
                responseModel.requestModel.isCustomEvent() &&
                responseModel.parsedBody.has("deviceEventState")
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val deviceEventState = responseModel.parsedBody.getString("deviceEventState")
        deviceEventStateStorage.set(deviceEventState)
    }
}