package com.emarsys.mobileengage.request;

import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageRequestContext;
import com.emarsys.mobileengage.endpoint.Endpoint;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.mobileengage.util.RequestPayloadUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MobileEngageRequestModelFactory {
    private MobileEngageRequestContext requestContext;
    private final ServiceEndpointProvider clientServiceProvider;
    private final ServiceEndpointProvider eventServiceProvider;
    private final ServiceEndpointProvider mobileEngageV2Provider;
    private final ServiceEndpointProvider inboxServiceProvider;

    public MobileEngageRequestModelFactory(MobileEngageRequestContext requestContext,
                                           ServiceEndpointProvider clientServiceProvider,
                                           ServiceEndpointProvider eventServiceProvider,
                                           ServiceEndpointProvider mobileEngageV2Provider,
                                           ServiceEndpointProvider inboxServiceProvider) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(clientServiceProvider, "ClientServiceProvider must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");
        Assert.notNull(inboxServiceProvider, "InboxServiceProvider must not be null!");
        Assert.notNull(mobileEngageV2Provider, "MobileEngageV2Provider must not be null!");

        this.requestContext = requestContext;
        this.clientServiceProvider = clientServiceProvider;
        this.eventServiceProvider = eventServiceProvider;
        this.mobileEngageV2Provider = mobileEngageV2Provider;
        this.inboxServiceProvider = inboxServiceProvider;
    }

    public RequestModel createSetPushTokenRequest(String pushToken) {
        Assert.notNull(pushToken, "PushToken must not be null!");

        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(clientServiceProvider.provideEndpointHost() + Endpoint.clientBase(requestContext.getApplicationCode()) + "/push-token")
                .method(RequestMethod.PUT)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(RequestPayloadUtils.createSetPushTokenPayload(pushToken))
                .build();
    }

    public RequestModel createRemovePushTokenRequest() {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(clientServiceProvider.provideEndpointHost() + Endpoint.clientBase(requestContext.getApplicationCode()) + "/push-token")
                .method(RequestMethod.DELETE)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .build();
    }

    public RequestModel createTrackDeviceInfoRequest() {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(clientServiceProvider.provideEndpointHost() + Endpoint.clientBase(requestContext.getApplicationCode()))
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(RequestPayloadUtils.createTrackDeviceInfoPayload(requestContext))
                .build();
    }

    public RequestModel createSetContactRequest(String contactFieldValue) {
        RequestModel.Builder builder = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(clientServiceProvider.provideEndpointHost() + Endpoint.clientBase(requestContext.getApplicationCode()) + "/contact")
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

        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(mobileEngageV2Provider.provideEndpointHost() + "events/message_open")
                .payload(RequestPayloadUtils.createTrackNotificationOpenPayload(sid, requestContext))
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext))
                .build();
    }

    public RequestModel createResetBadgeCountRequest() {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(inboxServiceProvider.provideEndpointHost() + "reset-badge-count")
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
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(clientServiceProvider.provideEndpointHost() + Endpoint.clientBase(requestContext.getApplicationCode()) + "/contact-token")
                .method(RequestMethod.POST)
                .headers(headers)
                .payload(RequestPayloadUtils.createRefreshContactTokenPayload(requestContext))
                .build();
    }

    private RequestModel createEvent(Map<String, Object> payload, MobileEngageRequestContext requestContext) {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(eventServiceProvider.provideEndpointHost() + Endpoint.eventBase(requestContext.getApplicationCode()))
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(payload)
                .build();
    }

    public RequestModel createFetchNotificationsRequest() {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(inboxServiceProvider.provideEndpointHost() + "notifications")
                .headers(RequestHeaderUtils.createInboxHeaders(requestContext))
                .method(RequestMethod.GET)
                .build();
    }
}
