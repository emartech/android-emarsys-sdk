package com.emarsys.mobileengage.util

import com.emarsys.core.endpoint.Endpoint
import com.emarsys.core.endpoint.ServiceEndpointProvider

object RequestUrlUtils {
    @JvmStatic
    fun isMobileEngageUrl(url: String,
                          clientServiceProvider: ServiceEndpointProvider,
                          eventServiceProvider: ServiceEndpointProvider,
                          eventServiceV4Provider: ServiceEndpointProvider,
                          messageInboxServiceProvider: ServiceEndpointProvider): Boolean {
        return url.startsWith(clientServiceProvider.provideEndpointHost())
                || url.startsWith(eventServiceProvider.provideEndpointHost())
                || url.startsWith(eventServiceV4Provider.provideEndpointHost())
                || url.startsWith(messageInboxServiceProvider.provideEndpointHost())
                || url.startsWith(Endpoint.REMOTE_CONFIG_URL)
    }

    @JvmStatic
    fun isCustomEvent(url: String, eventServiceProvider: ServiceEndpointProvider, eventServiceV4Provider: ServiceEndpointProvider): Boolean {
        return (url.startsWith(eventServiceProvider.provideEndpointHost()) || url.startsWith(eventServiceV4Provider.provideEndpointHost())) && url.endsWith("/events")
    }

    @JvmStatic
    fun isRefreshContactTokenUrl(url: String, clientServiceProvider: ServiceEndpointProvider): Boolean {
        return url.startsWith(clientServiceProvider.provideEndpointHost()) && url.endsWith("/contact-token")
    }
}