package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.RequestContext;

import java.util.HashMap;
import java.util.Map;

public class RequestHeaderUtils {

    public static Map<String, String> createBaseHeaders_V3(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("X-Client-Id", requestContext.getDeviceInfo().getHwid());
        baseHeaders.put("X-Request-Order", Long.toString(requestContext.getTimestampProvider().provideTimestamp()));

        return baseHeaders;
    }


    public static Map<String, String> createDefaultHeaders(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        HashMap<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("X-EMARSYS-SDK-VERSION", BuildConfig.VERSION_NAME);
        defaultHeaders.put("X-EMARSYS-SDK-MODE", requestContext.getDeviceInfo().isDebugMode() ? "debug" : "production");

        return defaultHeaders;
    }

}
