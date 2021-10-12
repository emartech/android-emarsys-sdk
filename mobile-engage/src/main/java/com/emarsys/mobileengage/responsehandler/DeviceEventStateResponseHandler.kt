package com.emarsys.mobileengage.responsehandler

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.util.RequestModelHelper

class DeviceEventStateResponseHandler(
    private val deviceEventStateStorage: Storage<String?>,
    private val requestModelHelper: RequestModelHelper
) : AbstractResponseHandler() {

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        return FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) &&
                responseModel.statusCode in 200..299 &&
                (requestModelHelper.isCustomEvent(responseModel.requestModel) ||
                        requestModelHelper.isInlineInAppRequest(responseModel.requestModel)) &&
                responseModel.parsedBody?.has("deviceEventState") ?: false
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val deviceEventState = responseModel.parsedBody!!.getString("deviceEventState")
        deviceEventStateStorage.set(deviceEventState)
    }
}