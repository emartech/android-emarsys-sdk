package com.emarsys.mobileengage.util;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.iam.model.IamConversionUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestPayloadUtils {

    public static Map<String, Object> createBasePayload(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", requestContext.getApplicationCode());
        payload.put("hardware_id", requestContext.getDeviceInfo().getHwid());

        if (requestContext.getContactFieldValueStorage().get() != null) {
            payload.put("contact_field_id", requestContext.getContactFieldId());
            payload.put("contact_field_value", requestContext.getContactFieldValueStorage().get());
        }
        return payload;
    }

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

    public static Map<String, Object> createSetContactPayload(String contactFieldValue, RequestContext requestContext) {
        Assert.notNull(contactFieldValue, "ContactFieldValue must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, Object> payload = new HashMap<>();
        payload.put("contactFieldId", requestContext.getContactFieldId());
        payload.put("contactFieldValue", contactFieldValue);

        return payload;
    }

    public static Map<String, Object> createCustomEventPayload(String eventName, Map<String, String> eventAttributes, RequestContext requestContext) {
        Assert.notNull(eventName, "EventName must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return createEventPayload(EventType.CUSTOM, eventName, eventAttributes, requestContext);
    }

    public static Map<String, Object> createInternalCustomEventPayload(String eventName, Map<String, String> eventAttributes, RequestContext requestContext) {
        Assert.notNull(eventName, "EventName must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return createEventPayload(EventType.INTERNAL, eventName, eventAttributes, requestContext);
    }

    private static Map<String, Object> createEventPayload(EventType eventType, String eventName, Map<String, String> eventAttributes, RequestContext requestContext) {
        Map<String, Object> event = createEvent(eventType, eventName, eventAttributes, requestContext);

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", Collections.emptyList());
        payload.put("viewedMessages", Collections.emptyList());
        payload.put("events", Collections.singletonList(event));

        return payload;
    }

    private static Map<String, Object> createEvent(EventType eventType, String eventName, Map<String, String> eventAttributes, RequestContext requestContext) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", eventType.name().toLowerCase());
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(requestContext.getTimestampProvider().provideTimestamp()));
        if (eventAttributes != null && !eventAttributes.isEmpty()) {
            event.put("attributes", eventAttributes);
        }
        return event;
    }


    public static Map<String, Object> createCompositeRequestModelPayload(
            List<?> events,
            List<DisplayedIam> displayedIams,
            List<ButtonClicked> buttonClicks,
            boolean doNotDisturb) {
        Assert.notNull(events, "Events must not be null!");
        Assert.notNull(displayedIams, "DisplayedIams must not be null!");
        Assert.notNull(buttonClicks, "ButtonClicks must not be null!");

        Map<String, Object> compositePayload = new HashMap<>();
        compositePayload.put("viewedMessages", IamConversionUtils.displayedIamsToArray(displayedIams));
        compositePayload.put("clicks", IamConversionUtils.buttonClicksToArray(buttonClicks));
        if (doNotDisturb) {
            compositePayload.put("dnd", true);
        }
        compositePayload.put("events", events);
        return compositePayload;
    }

    public static Map<String, Object> createRefreshContactTokenPayload(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, Object> payload = new HashMap<>();
        payload.put("refreshToken", requestContext.getRefreshTokenStorage().get());
        return payload;
    }

    public static Map<String, Object> createTrackNotificationOpenPayload(String sid, RequestContext requestContext) {
        Assert.notNull(sid, "Sid must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, Object> payload = createBasePayload(requestContext);
        payload.put("source", "inbox");
        payload.put("sid", sid);
        return payload;
    }
}

enum EventType {
    CUSTOM, INTERNAL
}