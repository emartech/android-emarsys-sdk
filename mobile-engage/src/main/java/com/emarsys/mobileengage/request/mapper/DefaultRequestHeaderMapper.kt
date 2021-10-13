package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext

class DefaultRequestHeaderMapper(
    override val requestContext: MobileEngageRequestContext) : AbstractRequestMapper(requestContext){

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean = true

    override fun createHeaders(requestModel: RequestModel): Map<String, String> {
        val defaultHeaders: MutableMap<String, String> = requestModel.headers.toMutableMap()

        defaultHeaders["Content-Type"] = "application/json"
        defaultHeaders["X-EMARSYS-SDK-VERSION"] = requestContext.deviceInfo.sdkVersion
        defaultHeaders["X-EMARSYS-SDK-MODE"] = if (requestContext.deviceInfo.isDebugMode) "debug" else "production"
        return defaultHeaders
    }
}