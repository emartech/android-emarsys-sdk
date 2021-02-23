package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest

class MobileEngageHeaderMapper(override val requestContext: MobileEngageRequestContext) : AbstractRequestMapper(requestContext) {

    override fun createHeaders(requestModel: RequestModel): Map<String, String> {
        val updatedHeaders: MutableMap<String, String> = requestModel.headers.toMutableMap()

        requestContext.clientStateStorage.get()?.let {
            updatedHeaders["X-Client-State"] = it
        }
        updatedHeaders["X-Request-Order"] = requestContext.timestampProvider.provideTimestamp().toString()
        return updatedHeaders
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return requestModel.isMobileEngageRequest()
    }
}