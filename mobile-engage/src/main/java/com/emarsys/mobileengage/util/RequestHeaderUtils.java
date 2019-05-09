package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.RequestContext;

import java.util.HashMap;
import java.util.Map;

public class RequestHeaderUtils {

    public static Map<String, String> createBaseHeaders_V3(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("X-Client-Id", requestContext.getDeviceInfo().getHwid());
        return baseHeaders;
    }

    public static Map<String, String> createDefaultHeaders(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        HashMap<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("X-EMARSYS-SDK-VERSION", requestContext.getDeviceInfo().getSdkVersion());
        defaultHeaders.put("X-EMARSYS-SDK-MODE", requestContext.getDeviceInfo().isDebugMode() ? "debug" : "production");

        return defaultHeaders;
    }

    public static Map<String, String> createBaseHeaders_V2(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("Authorization", HeaderUtils.createBasicAuth(
                requestContext.getApplicationCode(),
                requestContext.getApplicationPassword()));
        return baseHeaders;
    }

    public static Map<String, String> createInboxHeaders(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, String> headers = new HashMap<>();

        headers.put("x-ems-me-hardware-id", requestContext.getDeviceInfo().getHwid());
        headers.put("x-ems-me-application-code", requestContext.getApplicationCode());
        headers.put("x-ems-me-contact-field-id", String.valueOf(requestContext.getContactFieldId()));
        headers.put("x-ems-me-contact-field-value", requestContext.getContactFieldValueStorage().get());

        headers.putAll(createDefaultHeaders(requestContext));
        headers.putAll(createBaseHeaders_V2(requestContext));

        return headers;
    }
}
