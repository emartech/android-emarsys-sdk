package com.emarsys.mobileengage.request.mapper

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelHelper
import org.json.JSONObject

class DeviceEventStateRequestMapper(
        override val requestContext: MobileEngageRequestContext,
        override val requestModelHelper: RequestModelHelper,
        private val deviceEventStateStorage: Storage<String?>) : AbstractRequestMapper(requestContext, requestModelHelper) {

    override fun createPayload(requestModel: RequestModel): Map<String, Any?> {
        val updatedPayload: MutableMap<String, Any?> = requestModel.payload?.toMutableMap()
                ?: mutableMapOf()

        updatedPayload["deviceEventState"] = JSONObject(deviceEventStateStorage.get()!!)

        return updatedPayload
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return (requestModelHelper.isInlineInAppRequest(requestModel)
                || requestModelHelper.isCustomEvent(requestModel))
                && deviceEventStateStorage.get() != null
                && FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4)
    }
}