package com.emarsys.mobileengage.util

import com.emarsys.core.endpoint.Endpoint
import com.emarsys.core.endpoint.ServiceEndpointProvider

object RequestUrlUtils {
    @JvmStatic
    fun isMobileEngageV3Url(url: String,
                            clientServiceProvider: ServiceEndpointProvider,
                            eventServiceProvider: ServiceEndpointProvider,
                            messageInboxServiceProvider: ServiceEndpointProvider): Boolean {
        return url.startsWith(clientServiceProvider.provideEndpointHost())
                || url.startsWith(eventServiceProvider.provideEndpointHost())
                || url.startsWith(messageInboxServiceProvider.provideEndpointHost())
                || url.startsWith(Endpoint.REMOTE_CONFIG_URL)
    }

    @JvmStatic
    fun isCustomEvent_V3(url: String, eventServiceProvider: ServiceEndpointProvider): Boolean {
        return url.startsWith(eventServiceProvider.provideEndpointHost()) && url.endsWith("/events")
    }

    @JvmStatic
    fun isRefreshContactTokenUrl(url: String, clientServiceProvider: ServiceEndpointProvider): Boolean {
        return url.startsWith(clientServiceProvider.provideEndpointHost()) && url.endsWith("/contact-token")
    }
}