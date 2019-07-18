package com.emarsys.mobileengage.request;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageRequestContext;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.mobileengage.util.RequestPayloadUtils;
import com.emarsys.mobileengage.util.RequestUrlUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.endpoint.Endpoint.INBOX_FETCH_V1;
import static com.emarsys.mobileengage.endpoint.Endpoint.INBOX_RESET_BADGE_COUNT_V1;

public class MobileEngageRequestModelFactory {
    private MobileEngageRequestContext requestContext;

    public MobileEngageRequestModelFactory(MobileEngageRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        this.requestContext = requestContext;
    }

    public RequestModel createSetPushTokenRequest(String pushToken) {
        Assert.notNull(pushToken, "PushToken must not be null!");

        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createSetPushTokenUrl(requestContext))
                .method(RequestMethod.PUT)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(RequestPayloadUtils.createSetPushTokenPayload(pushToken))
                .build();
    }

    public RequestModel createRemovePushTokenRequest() {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createRemovePushTokenUrl(requestContext))
                .method(RequestMethod.DELETE)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .build();
    }

    public RequestModel createTrackDeviceInfoRequest() {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createTrackDeviceInfoUrl(requestContext))
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(RequestPayloadUtils.createTrackDeviceInfoPayload(requestContext))
                .build();
    }

    public RequestModel createSetContactRequest(String contactFieldValue) {
        RequestModel.Builder builder = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createSetContactUrl(requestContext))
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext));
        if (contactFieldValue == null) {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("anonymous", "true");
            builder.payload(Collections.<String, Object>emptyMap());
            builder.queryParams(queryParams);
        } else {
            builder.payload(RequestPayloadUtils.createSetContactPayload(contactFieldValue, requestContext));
        }
        return builder.build();
    }

    public RequestModel createTrackNotificationOpenRequest(String sid) {
        Assert.notNull(sid, "Sid must not be null!");

        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createEventUrl_V2("message_open"))
                .payload(RequestPayloadUtils.createTrackNotificationOpenPayload(sid, requestContext))
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext))
                .build();
    }

    public RequestModel createResetBadgeCountRequest() {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(INBOX_RESET_BADGE_COUNT_V1)
                .headers(RequestHeaderUtils.createInboxHeaders(requestContext))
                .method(RequestMethod.POST)
                .build();
    }

    public RequestModel createCustomEventRequest(String eventName, Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");

        Map<String, Object> payload = RequestPayloadUtils.createCustomEventPayload(eventName, eventAttributes, requestContext);

        return createEvent(payload, requestContext);
    }

    public RequestModel createInternalCustomEventRequest(String eventName, Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");

        Map<String, Object> payload = RequestPayloadUtils.createInternalCustomEventPayload(eventName, eventAttributes, requestContext);

        return createEvent(payload, requestContext);
    }

    public RequestModel createRefreshContactTokenRequest() {
        Map<String, String> headers = new HashMap<>();
        headers.putAll(RequestHeaderUtils.createBaseHeaders_V3(requestContext));
        headers.putAll(RequestHeaderUtils.createDefaultHeaders(requestContext));
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createRefreshContactTokenUrl(requestContext))
                .method(RequestMethod.POST)
                .headers(headers)
                .payload(RequestPayloadUtils.createRefreshContactTokenPayload(requestContext))
                .build();
    }

    private static RequestModel createEvent(Map<String, Object> payload, MobileEngageRequestContext requestContext) {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createCustomEventUrl(requestContext))
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(payload)
                .build();
    }

    public RequestModel createFetchNotificationsRequest() {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(INBOX_FETCH_V1)
                .headers(RequestHeaderUtils.createInboxHeaders(requestContext))
                .method(RequestMethod.GET)
                .build();
    }
}
