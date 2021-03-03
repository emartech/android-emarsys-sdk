package com.emarsys.mobileengage.request.mapper

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelUtils.isCustomEvent
import com.emarsys.mobileengage.util.RequestModelUtils.isInlineInAppRequest

class DeviceEventStateRequestMapper(
        override val requestContext: MobileEngageRequestContext,
        private val deviceEventStateStorage: Storage<String?>) : AbstractRequestMapper(requestContext) {

    override fun createPayload(requestModel: RequestModel): Map<String, Any?> {
        val updatedPayload: MutableMap<String, Any?> = requestModel.payload?.toMutableMap()
                ?: mutableMapOf()

        updatedPayload["deviceEventState"] = deviceEventStateStorage.get()

        return updatedPayload
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return (requestModel.isInlineInAppRequest()
                || requestModel.isCustomEvent())
                && deviceEventStateStorage.get() != null
                && FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4)
    }
}