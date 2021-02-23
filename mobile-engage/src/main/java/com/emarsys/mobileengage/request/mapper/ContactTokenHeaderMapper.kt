package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageSetContactRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isRefreshContactTokenRequest

class ContactTokenHeaderMapper(override val requestContext: MobileEngageRequestContext) : AbstractRequestMapper(requestContext) {

    override fun createHeaders(requestModel: RequestModel): Map<String, String> {
        val headers: MutableMap<String, String> = requestModel.headers.toMutableMap()

        headers["X-Contact-Token"] = requestContext.contactTokenStorage.get()
        return headers
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return requestModel.isMobileEngageRequest()
                && !requestModel.isRefreshContactTokenRequest()
                && !requestModel.isMobileEngageSetContactRequest()
                && requestContext.contactTokenStorage.get() != null
    }
}