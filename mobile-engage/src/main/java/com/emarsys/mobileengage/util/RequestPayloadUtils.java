package com.emarsys.mobileengage.util;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;

import java.util.HashMap;
import java.util.Map;

public class RequestPayloadUtils {
    public static Map<String, Object> createSetPushTokenPayload(String pushToken) {
        Assert.notNull(pushToken, "PushToken must not be null!");

        Map<String, Object> payload = new HashMap<>();
        payload.put("pushToken", pushToken);
        return payload;
    }

    public static Map<String, Object> createTrackDeviceInfoPayload(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        DeviceInfo deviceInfo = requestContext.getDeviceInfo();

        Map<String, Object> payload = new HashMap<>();
        payload.put("platform", deviceInfo.getPlatform());
        payload.put("applicationVersion", deviceInfo.getApplicationVersion());
        payload.put("deviceModel", deviceInfo.getModel());
        payload.put("osVersion", deviceInfo.getOsVersion());
        payload.put("sdkVersion", deviceInfo.getSdkVersion());
        payload.put("language", deviceInfo.getLanguage());
        payload.put("timezone", deviceInfo.getTimezone());
        return payload;
    }
}
