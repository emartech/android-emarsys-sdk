package com.emarsys.mobileengage.util

import com.emarsys.core.di.getDependency
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.endpoint.Endpoint

object RequestModelUtils {
    fun ResponseModel.isMobileEngageRequest() = requestModel.isMobileEngageRequest()
    fun ResponseModel.isCustomEvent(): Boolean = requestModel.isCustomEvent()
    fun RequestModel.isMobileEngageRequest(): Boolean {
        val clientServiceUrl = (getDependency(Endpoint.ME_V3_CLIENT_HOST) as ServiceEndpointProvider).provideEndpointHost()
        val eventServiceUrl = (getDependency(Endpoint.ME_V3_EVENT_HOST) as ServiceEndpointProvider).provideEndpointHost()
        val eventServiceV4Url = (getDependency(Endpoint.ME_V4_EVENT_HOST) as ServiceEndpointProvider).provideEndpointHost()
        val messageInboxServiceUrl = (getDependency(Endpoint.ME_V3_INBOX_HOST) as ServiceEndpointProvider).provideEndpointHost()

        val url = this.url.toString()
        return url.startsWithOneOf(clientServiceUrl, eventServiceUrl, eventServiceV4Url, messageInboxServiceUrl)
    }

    fun RequestModel.isRemoteConfigRequest(): Boolean {
        val url = this.url.toString()

        return url.startsWith(com.emarsys.core.endpoint.Endpoint.REMOTE_CONFIG_URL)
    }

    fun RequestModel.isCustomEvent(): Boolean {
        val eventServiceUrl = (getDependency(Endpoint.ME_V3_EVENT_HOST)as ServiceEndpointProvider).provideEndpointHost()
        val eventServiceV4Url = (getDependency(Endpoint.ME_V4_EVENT_HOST)as ServiceEndpointProvider).provideEndpointHost()

        val url = this.url.toString()
        return url.startsWithOneOf(eventServiceUrl, eventServiceV4Url) && url.endsWith("/events")
    }

    fun RequestModel.isRefreshContactTokenRequest(): Boolean {
        val clientServiceUrl = (getDependency(Endpoint.ME_V3_CLIENT_HOST) as ServiceEndpointProvider).provideEndpointHost()

        val url = this.url.toString()
        return url.startsWithOneOf(clientServiceUrl) && url.endsWith("/contact-token")
    }

    private fun String.startsWithOneOf(vararg patterns: String): Boolean {
        return patterns.any { this.startsWith(it) }
    }
}