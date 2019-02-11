package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;

import java.util.HashMap;
import java.util.Map;

public class RequestHeaderUtils {

    public static Map<String, String> createBaseHeaders_V3(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("X-CLIENT-ID", requestContext.getDeviceInfo().getHwid());
        baseHeaders.put("X-REQUEST-ORDER", Long.toString(requestContext.getTimestampProvider().provideTimestamp()));

        return baseHeaders;
    }
}
