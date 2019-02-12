package com.emarsys.mobileengage.util;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;

public class RequestModelUtils {

    public static RequestModel createSetPushTokenRequest(String pushToken, RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(pushToken, "PushToken must not be null!");

        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createSetPushTokenUrl(requestContext))
                .method(RequestMethod.PUT)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(RequestPayloadUtils.createSetPushTokenPayload(pushToken))
                .build();
    }

    public static RequestModel createTrackDeviceInfoRequest(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createTrackDeviceInfoUrl(requestContext))
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(RequestPayloadUtils.createTrackDeviceInfoPayload(requestContext))
                .build();
    }
}
