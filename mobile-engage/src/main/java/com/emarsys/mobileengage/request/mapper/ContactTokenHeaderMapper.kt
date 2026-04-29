package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelHelper

class ContactTokenHeaderMapper(
    override val requestContext: MobileEngageRequestContext,
    override val requestModelHelper: RequestModelHelper
) : AbstractRequestMapper(requestContext, requestModelHelper) {

    override fun createHeaders(requestModel: RequestModel): Map<String, String> {
        val headers: MutableMap<String, String> = requestModel.headers.toMutableMap()
        requestContext.contactTokenStorage.get()?.let {
            headers["X-Contact-Token"] = it
        }
        return headers
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return (isNotSetContactAndNotRefreshContactTokenMobileEngageRequest(requestModel)
                || isPredictRequest(requestModel))
                && isContactTokenAvailable()
    }

    private fun isNotSetContactAndNotRefreshContactTokenMobileEngageRequest(requestModel: RequestModel) =
        (requestModelHelper.isMobileEngageRequest(requestModel)
                && !requestModelHelper.isMobileEngageRefreshContactTokenRequest(requestModel)
                && !requestModelHelper.isMobileEngageSetContactRequest(requestModel))

    private fun isPredictRequest(requestModel: RequestModel) =
        requestModelHelper.isPredictRequest(requestModel)

    private fun isContactTokenAvailable() = requestContext.contactTokenStorage.get() != null
}