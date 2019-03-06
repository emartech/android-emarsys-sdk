package com.emarsys.mobileengage.util;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;

import java.util.Map;

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

    public static RequestModel createSetContactRequest(String contactFieldValue, RequestContext requestContext) {
        Assert.notNull(contactFieldValue, "ContactFieldValue must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createSetContactUrl(requestContext))
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(RequestPayloadUtils.createSetContactPayload(contactFieldValue, requestContext))
                .build();
    }

    public static RequestModel createCustomEventRequest(String eventName, Map<String, String> eventAttributes, RequestContext requestContext) {
        Assert.notNull(eventName, "EventName must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, Object> payload = RequestPayloadUtils.createCustomEventPayload(eventName, eventAttributes, requestContext);

        return createEvent(payload, requestContext);
    }

    public static RequestModel createInternalCustomEventRequest(String eventName, Map<String, String> eventAttributes, RequestContext requestContext) {
        Assert.notNull(eventName, "EventName must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, Object> payload = RequestPayloadUtils.createInternalCustomEventPayload(eventName, eventAttributes, requestContext);

        return createEvent(payload, requestContext);
    }

    private static RequestModel createEvent(Map<String, Object> payload, RequestContext requestContext) {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createCustomEventUrl(requestContext))
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(payload)
                .build();
    }
}
