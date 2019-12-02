package com.emarsys.mobileengage.util;

import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;


public class RequestModelUtils {

    public static boolean isMobileEngageV3Request(RequestModel requestModel, ServiceEndpointProvider clientServiceProvider, ServiceEndpointProvider eventServiceProvider) {
        Assert.notNull(requestModel, "RequestModel must not be null!");
        Assert.notNull(clientServiceProvider, "ClientServiceProvider must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");

        String url = requestModel.getUrl().toString();

        return RequestUrlUtils.isMobileEngageV3Url(url, clientServiceProvider, eventServiceProvider);
    }

    public static boolean isCustomEvent_V3(RequestModel requestModel, ServiceEndpointProvider eventServiceProvider) {
        Assert.notNull(requestModel, "RequestModel must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");
        String url = requestModel.getUrl().toString();

        return RequestUrlUtils.isCustomEvent_V3(url, eventServiceProvider);
    }

    public static boolean isRefreshContactTokenRequest(RequestModel requestModel, ServiceEndpointProvider clientServiceProvider) {
        Assert.notNull(requestModel, "RequestModel must not be null!");
        Assert.notNull(clientServiceProvider, "ClientServiceProvider must not be null!");

        String url = requestModel.getUrl().toString();

        return RequestUrlUtils.isRefreshContactTokenUrl(url, clientServiceProvider);
    }
}
