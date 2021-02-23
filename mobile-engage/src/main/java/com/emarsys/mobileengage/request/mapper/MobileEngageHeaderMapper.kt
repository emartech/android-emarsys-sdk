package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.Mapper
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest

class MobileEngageHeaderMapper(val requestContext: MobileEngageRequestContext) : Mapper<RequestModel, RequestModel> {
    override fun map(requestModel: RequestModel): RequestModel {
        var updatedRequestModel = requestModel
        if (requestModel.isMobileEngageRequest()) {

            val updatedHeaders: MutableMap<String, String> = requestModel.headers.toMutableMap()

            requestContext.clientStateStorage.get()?.let {
                updatedHeaders["X-Client-State"] = it
            }
            updatedHeaders["X-Request-Order"] = requestContext.timestampProvider.provideTimestamp().toString()

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

}