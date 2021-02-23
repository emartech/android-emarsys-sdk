package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageSetContactRequest

class OpenIdTokenRequestMapper(override val requestContext: MobileEngageRequestContext) : AbstractRequestMapper(requestContext) {

    override fun createHeaders(requestModel: RequestModel): Map<String, String> {
        val updatedHeaders: MutableMap<String, String> = requestModel.headers.toMutableMap()

        updatedHeaders["X-Open-Id"] = requestContext.openIdToken!!

        return updatedHeaders
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return requestModel.isMobileEngageRequest()
                && requestModel.isMobileEngageSetContactRequest()
                && requestContext.openIdToken != null
    }
}