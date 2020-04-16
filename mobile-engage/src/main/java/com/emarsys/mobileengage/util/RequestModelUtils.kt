package com.emarsys.mobileengage.util

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.util.RequestUrlUtils.isCustomEvent_V3
import com.emarsys.mobileengage.util.RequestUrlUtils.isMobileEngageV3Url
import com.emarsys.mobileengage.util.RequestUrlUtils.isRefreshContactTokenUrl

object RequestModelUtils {
    @JvmStatic
    fun isMobileEngageV3Request(requestModel: RequestModel,
                                clientServiceProvider: ServiceEndpointProvider,
                                eventServiceProvider: ServiceEndpointProvider,
                                messageInboxServiceProvider: ServiceEndpointProvider): Boolean {
        val url = requestModel.url.toString()
        return isMobileEngageV3Url(url, clientServiceProvider, eventServiceProvider, messageInboxServiceProvider)
    }

    @JvmStatic
    fun isCustomEvent_V3(requestModel: RequestModel, eventServiceProvider: ServiceEndpointProvider): Boolean {
        val url = requestModel.url.toString()
        return isCustomEvent_V3(url, eventServiceProvider)
    }

    @JvmStatic
    fun isRefreshContactTokenRequest(requestModel: RequestModel, clientServiceProvider: ServiceEndpointProvider): Boolean {
        val url = requestModel.url.toString()
        return isRefreshContactTokenUrl(url, clientServiceProvider)
    }
}