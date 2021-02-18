package com.emarsys.mobileengage.request

import com.emarsys.core.Mapper
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.util.Assert
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageSetContactRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isRefreshContactTokenRequest
import java.util.*

class MobileEngageHeaderMapper(val requestContext: MobileEngageRequestContext) : Mapper<RequestModel, RequestModel> {

    override fun map(requestModel: RequestModel): RequestModel {
        Assert.notNull(requestModel, "RequestModel must not be null!")
        val headersToInject = getHeadersToInject(requestModel)
        var updatedRequestModel = requestModel
        if (requestModel.isMobileEngageRequest()) {
            val updatedHeaders: MutableMap<String, String> = HashMap(requestModel.headers)
            updatedHeaders.putAll(headersToInject)
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

    private fun getHeadersToInject(requestModel: RequestModel): Map<String, String> {
        val headersToInject: MutableMap<String, String> = HashMap()
        requestContext.clientStateStorage.get()?.let {
            headersToInject["X-Client-State"] = it
        }
        requestContext.contactTokenStorage.get()?.let {
            if (!requestModel.isRefreshContactTokenRequest() && !requestModel.isMobileEngageSetContactRequest()) {
                headersToInject["X-Contact-Token"] = it
            }
        }
        requestContext.idToken?.let {
            if (requestModel.isMobileEngageSetContactRequest()) {
                headersToInject["X-Open-Id"] = it
            }
        }
        headersToInject["X-Request-Order"] = requestContext.timestampProvider.provideTimestamp().toString()
        return headersToInject
    }

}