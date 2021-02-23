package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.Mapper
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageSetContactRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isRefreshContactTokenRequest

class ContactTokenHeaderMapper(val requestContext: MobileEngageRequestContext) : Mapper<RequestModel, RequestModel> {
    override fun map(requestModel: RequestModel): RequestModel {
        var updatedRequestModel = requestModel
        val contactToken = requestContext.contactTokenStorage.get()

        if (shouldHandleRequest(requestModel, contactToken)) {
            val updatedHeaders: MutableMap<String, String> = requestModel.headers.toMutableMap()

            updatedHeaders["X-Contact-Token"] = contactToken
            updatedRequestModel = if (updatedRequestModel is CompositeRequestModel) {
                CompositeRequestModel.Builder(requestModel)
                        .headers(updatedHeaders)
                        .build()
            } else {
                RequestModel.Builder(requestModel)
                        .headers(updatedHeaders)
                        .build()
            }
        }
        return updatedRequestModel
    }

    private fun shouldHandleRequest(requestModel: RequestModel, contactToken: String?) : Boolean {
        return requestModel.isMobileEngageRequest()
                && !requestModel.isRefreshContactTokenRequest()
                && !requestModel.isMobileEngageSetContactRequest()
                && contactToken != null
    }

}