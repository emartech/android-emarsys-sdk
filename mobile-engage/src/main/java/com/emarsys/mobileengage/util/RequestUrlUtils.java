package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.endpoint.Endpoint;

public class RequestUrlUtils {
    public static String createSetPushTokenUrl(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE + "/push-token", requestContext.getApplicationCode());
    }

    public static String createTrackDeviceInfoUrl(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE, requestContext.getApplicationCode());
    }

    public static String createSetContactUrl(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE + "/contact", requestContext.getApplicationCode());
    }
}
