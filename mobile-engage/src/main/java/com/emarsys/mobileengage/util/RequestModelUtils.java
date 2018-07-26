package com.emarsys.mobileengage.util;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.endpoint.Endpoint.ME_LAST_MOBILE_ACTIVITY_V2;
import static com.emarsys.mobileengage.endpoint.Endpoint.ME_LOGIN_V2;

public class RequestModelUtils {

    public static boolean isCustomEvent_V3(RequestModel requestModel) {
        Assert.notNull(requestModel, "RequestModel must not be null");
        String url = requestModel.getUrl().toString();
        return RequestUrlUtils.isCustomEvent_V3(url);
    }

    public static RequestModel createAppLogin_V2(RequestContext requestContext,
                                                 String pushToken) {
        Assert.notNull(requestContext, "RequestContext must not be null");

        Map<String, Object> payload = RequestPayloadUtils.createAppLoginPayload(requestContext, pushToken);

        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                .url(ME_LOGIN_V2)
                .payload(payload)
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext.getConfig()))
                .build();
    }

    public static RequestModel createLastMobileActivity(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null");
        RequestModel result;
        if (MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            result = createInternalCustomEvent(
                    "last_mobile_activity",
                    null,
                    requestContext);
        } else {
            result = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                    .url(ME_LAST_MOBILE_ACTIVITY_V2)
                    .payload(RequestPayloadUtils.createBasePayload(requestContext))
                    .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext.getConfig()))
                    .build();
        }
        return result;
    }

    public static RequestModel createInternalCustomEvent(
            String eventName,
            Map<String, String> attributes,
            RequestContext requestContext) {
        Assert.notNull(eventName, "EventName must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, Object> event = new HashMap<>();
        event.put("type", "internal");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(requestContext.getTimestampProvider().provideTimestamp()));
        if (attributes != null && !attributes.isEmpty()) {
            event.put("attributes", attributes);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", Collections.emptyList());
        payload.put("viewed_messages", Collections.emptyList());
        payload.put("events", Collections.singletonList(event));

        return new RequestModel(
                RequestUrlUtils.createEventUrl_V3(requestContext.getMeIdStorage().get()),
                RequestMethod.POST,
                payload,
                RequestHeaderUtils.createBaseHeaders_V3(requestContext),
                requestContext.getTimestampProvider().provideTimestamp(),
                Long.MAX_VALUE,
                requestContext.getRequestIdProvider().provideId());
    }

}
