package com.emarsys.mobileengage.util;

import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.util.Assert;

public class RequestUrlUtils {

    public static boolean isMobileEngageV3Url(String url, ServiceEndpointProvider clientServiceProvider, ServiceEndpointProvider eventServiceProvider) {
        Assert.notNull(url, "Url must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");
        Assert.notNull(clientServiceProvider, "ClientServiceProvider must not be null!");

        return url.startsWith(clientServiceProvider.provideEndpointHost()) || url.startsWith(eventServiceProvider.provideEndpointHost());
    }

    public static boolean isCustomEvent_V3(String url, ServiceEndpointProvider eventServiceProvider) {
        Assert.notNull(url, "Url must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");

        return url.startsWith(eventServiceProvider.provideEndpointHost()) && url.endsWith("/events");
    }

    public static boolean isRefreshContactTokenUrl(String url, ServiceEndpointProvider clientServiceProvider) {
        Assert.notNull(url, "Url must not be null!");
        Assert.notNull(clientServiceProvider, "ClientServiceProvider must not be null!");

        return url.startsWith(clientServiceProvider.provideEndpointHost()) && url.endsWith("/contact-token");
    }
}
