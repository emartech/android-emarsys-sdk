package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelHelper

class MobileEngageHeaderMapper(override val requestContext: MobileEngageRequestContext,
                               override val requestModelHelper: RequestModelHelper) : AbstractRequestMapper(requestContext, requestModelHelper) {

    override fun createHeaders(requestModel: RequestModel): Map<String, String> {
        val updatedHeaders: MutableMap<String, String> = requestModel.headers.toMutableMap()

        requestContext.clientStateStorage.get()?.let {
            updatedHeaders["X-Client-State"] = it
        }
        updatedHeaders["X-Request-Order"] = requestContext.timestampProvider.provideTimestamp().toString()
        updatedHeaders["X-Client-Id"] = requestContext.deviceInfo.hardwareId
        return updatedHeaders
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return requestModelHelper.isMobileEngageRequest(requestModel)
    }
}