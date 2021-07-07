package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.MobileEngageRequestContext;

import java.util.HashMap;
import java.util.Map;

public class RequestHeaderUtils {

    public static Map<String, String> createBaseHeaders_V3(MobileEngageRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("X-Client-Id", requestContext.getDeviceInfo().getHardwareId());
        return baseHeaders;
    }

    public static Map<String, String> createDefaultHeaders(MobileEngageRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        HashMap<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("X-EMARSYS-SDK-VERSION", requestContext.getDeviceInfo().getSdkVersion());
        defaultHeaders.put("X-EMARSYS-SDK-MODE", requestContext.getDeviceInfo().isDebugMode() ? "debug" : "production");

        return defaultHeaders;
    }
}
