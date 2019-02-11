package com.emarsys.mobileengage.util;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.endpoint.Endpoint;

import java.util.HashMap;
import java.util.Map;

public class RequestModelUtils {

    public static RequestModel createSetPushTokenRequest(String pushToken, RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(pushToken, "PushToken must not be null!");
        Map<String, Object> payload = new HashMap<>();
        payload.put("pushToken", pushToken);
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(String.format(Endpoint.ME_V3_CLIENT_BASE + "push-token", requestContext.getApplicationCode()))
                .method(RequestMethod.PUT)
                .headers(createBaseHeaders_V3(requestContext))
                .payload(payload)
                .build();
    }


    private static Map<String, String> createBaseHeaders_V3(RequestContext requestContext) {
        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("X-CLIENT-ID", requestContext.getDeviceInfo().getHwid());
        baseHeaders.put("X-REQUEST-ORDER", Long.toString(requestContext.getTimestampProvider().provideTimestamp()));
        return baseHeaders;
    }

}
