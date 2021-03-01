package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageSetContactRequest

class OpenIdTokenRequestMapper(override val requestContext: MobileEngageRequestContext) : AbstractRequestMapper(requestContext) {

    override fun createPayload(requestModel: RequestModel): Map<String, Any?> {
        val updatedPayload: MutableMap<String, Any?> = requestModel.payload?.toMutableMap() ?: mutableMapOf()

        updatedPayload["openIdToken"] = requestContext.openIdToken

        return updatedPayload
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return requestModel.isMobileEngageRequest()
                && requestModel.isMobileEngageSetContactRequest()
                && requestContext.openIdToken != null
    }
}