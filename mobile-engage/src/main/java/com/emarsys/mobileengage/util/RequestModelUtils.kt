package com.emarsys.mobileengage.util

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.util.RequestUrlUtils.isCustomEvent
import com.emarsys.mobileengage.util.RequestUrlUtils.isMobileEngageUrl
import com.emarsys.mobileengage.util.RequestUrlUtils.isRefreshContactTokenUrl

object RequestModelUtils {
    @JvmStatic
    fun isMobileEngageRequest(requestModel: RequestModel,
                              clientServiceProvider: ServiceEndpointProvider,
                              eventServiceProvider: ServiceEndpointProvider,
                              eventServiceV4Provider: ServiceEndpointProvider,
                              messageInboxServiceProvider: ServiceEndpointProvider): Boolean {
        val url = requestModel.url.toString()
        return isMobileEngageUrl(url, clientServiceProvider, eventServiceProvider, eventServiceV4Provider, messageInboxServiceProvider)
    }

    @JvmStatic
    fun isCustomEvent(requestModel: RequestModel, eventServiceProvider: ServiceEndpointProvider, eventServiceV4Provider: ServiceEndpointProvider): Boolean {
        val url = requestModel.url.toString()
        return isCustomEvent(url, eventServiceProvider, eventServiceV4Provider)
    }

    @JvmStatic
    fun isRefreshContactTokenRequest(requestModel: RequestModel, clientServiceProvider: ServiceEndpointProvider): Boolean {
        val url = requestModel.url.toString()
        return isRefreshContactTokenUrl(url, clientServiceProvider)
    }
}