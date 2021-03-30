package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelHelper

class OpenIdTokenRequestMapper(override val requestContext: MobileEngageRequestContext,
                               override val requestModelHelper: RequestModelHelper) : AbstractRequestMapper(requestContext, requestModelHelper) {

    override fun createPayload(requestModel: RequestModel): Map<String, Any?> {
        val updatedPayload: MutableMap<String, Any?> = requestModel.payload?.toMutableMap() ?: mutableMapOf()

        updatedPayload["openIdToken"] = requestContext.openIdToken

        return updatedPayload
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return requestModelHelper.isMobileEngageRequest(requestModel)
                && requestModelHelper.isMobileEngageSetContactRequest(requestModel)
                && requestContext.openIdToken != null
    }
}