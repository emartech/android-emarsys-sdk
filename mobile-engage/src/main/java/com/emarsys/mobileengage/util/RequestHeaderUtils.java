package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.RequestContext;

import java.util.HashMap;
import java.util.Map;

public class RequestHeaderUtils {

    public static Map<String, String> createBaseHeaders_V2(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("Authorization", HeaderUtils.createBasicAuth(
                requestContext.getApplicationCode(),
                requestContext.getApplicationPassword()));
        return baseHeaders;
    }

    public static Map<String, String> createBaseHeaders_V3(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("X-ME-ID", requestContext.getMeIdStorage().get());
        baseHeaders.put("X-ME-ID-SIGNATURE", requestContext.getMeIdSignatureStorage().get());
        baseHeaders.put("X-ME-APPLICATIONCODE", requestContext.getApplicationCode());

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
