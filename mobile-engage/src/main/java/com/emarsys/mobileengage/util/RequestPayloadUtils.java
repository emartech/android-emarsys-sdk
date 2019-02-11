package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class RequestPayloadUtils {
    public static Map<String, Object> createSetPushTokenPayload(String pushToken) {
        Assert.notNull(pushToken, "PushToken must not be null!");

        Map<String, Object> payload = new HashMap<>();
        payload.put("pushToken", pushToken);
        return payload;
    }
}
