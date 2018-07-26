package com.emarsys.mobileengage.util;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.iam.model.IamConversionUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestPayloadUtils {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> createBasePayload(RequestContext requestContext) {
        return createBasePayload(Collections.EMPTY_MAP, requestContext);
    }

    public static Map<String, Object> createBasePayload(Map<String, Object> additionalPayload, RequestContext requestContext) {
        Assert.notNull(additionalPayload, "AdditionalPayload must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", requestContext.getApplicationCode());
        payload.put("hardware_id", requestContext.getDeviceInfo().getHwid());

        AppLoginParameters parameters = requestContext.getAppLoginParameters();
        if (parameters != null && parameters.hasCredentials()) {
            payload.put("contact_field_id", parameters.getContactFieldId());
            payload.put("contact_field_value", parameters.getContactFieldValue());
        }

        for (Map.Entry<String, Object> entry : additionalPayload.entrySet()) {
            payload.put(entry.getKey(), entry.getValue());
        }
        return payload;
    }

    public static Map<String, Object> createAppLoginPayload(
            RequestContext requestContext,
            String pushToken) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(requestContext);

        payload.put("platform", requestContext.getDeviceInfo().getPlatform());
        payload.put("language", requestContext.getDeviceInfo().getLanguage());
        payload.put("timezone", requestContext.getDeviceInfo().getTimezone());
        payload.put("device_model", requestContext.getDeviceInfo().getModel());
        payload.put("application_version", requestContext.getDeviceInfo().getApplicationVersion());
        payload.put("os_version", requestContext.getDeviceInfo().getOsVersion());
        payload.put("ems_sdk", MobileEngageInternal.MOBILEENGAGE_SDK_VERSION);

        if (pushToken == null) {
            payload.put("push_token", false);
        } else {
            payload.put("push_token", pushToken);
        }

        return payload;
    }

    public static Map<String, Object> createCompositeRequestModelPayload(
            List<?> events,
            List<DisplayedIam> displayedIams,
            List<ButtonClicked> buttonClicks,
            DeviceInfo deviceInfo,
            boolean doNotDisturb) {
        Assert.notNull(events, "Events must not be null!");
        Assert.notNull(displayedIams, "DisplayedIams must not be null!");
        Assert.notNull(buttonClicks, "ButtonClicks must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");

        Map<String, Object> compositePayload = new HashMap<>();
        compositePayload.put("viewed_messages", IamConversionUtils.displayedIamsToArray(displayedIams));
        compositePayload.put("clicks", IamConversionUtils.buttonClicksToArray(buttonClicks));
        if (doNotDisturb) {
            compositePayload.put("dnd", true);
        }
        compositePayload.put("events", events);
        compositePayload.put("hardware_id", deviceInfo.getHwid());
        compositePayload.put("language", deviceInfo.getLanguage());
        compositePayload.put("application_version", deviceInfo.getApplicationVersion());
        compositePayload.put("ems_sdk", MobileEngageInternal.MOBILEENGAGE_SDK_VERSION);
        return compositePayload;
    }
}
