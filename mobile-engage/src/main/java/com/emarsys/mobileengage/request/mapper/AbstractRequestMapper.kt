package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.Mapper
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext

abstract class AbstractRequestMapper(open val requestContext: MobileEngageRequestContext) : Mapper<RequestModel, RequestModel> {
    override fun map(requestModel: RequestModel): RequestModel {
        var updatedRequestModel = requestModel

        if (shouldMapRequestModel(requestModel)) {
            val updatedHeaders = createHeaders(requestModel)
            val updatedPayload = createPayload(requestModel)

            updatedRequestModel = if (updatedRequestModel is CompositeRequestModel) {
                with(CompositeRequestModel.Builder(requestModel)) {
                    headers(updatedHeaders)
                    updatedPayload?.let { payload(it) }
                    build()
                }
            } else {
                with(RequestModel.Builder(requestModel)) {
                    headers(updatedHeaders)
                    updatedPayload?.let { payload(it) }
                    build()
                }
            }
        }
        return updatedRequestModel
    }

    open fun createPayload(requestModel: RequestModel): Map<String, Any?>? {
        return requestModel.payload
    }

    open fun createHeaders(requestModel: RequestModel): Map<String, String> {
        return requestModel.headers
    }

    abstract fun shouldMapRequestModel(requestModel: RequestModel): Boolean
}