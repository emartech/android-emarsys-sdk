package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.emarsys.mobileengage.endpoint.Endpoint.ME_BASE_V2;
import static com.emarsys.mobileengage.endpoint.Endpoint.ME_BASE_V3;

public class RequestUrlUtils {
    private static Pattern customEventPattern = Pattern.compile("^" + ME_BASE_V3 + "\\w+/events$");

    public static String createEventUrl_V2(String eventName) {
        Assert.notNull(eventName, "EventName must not be null!");
        return ME_BASE_V2 + "events/" + eventName;
    }

    public static String createEventUrl_V3(String meId) {
        Assert.notNull(meId, "MEID must not be null!");

        return ME_BASE_V3 + meId + "/events";
    }

    public static boolean isCustomEvent_V3(String url) {
        Assert.notNull(url, "Url must not be null");
        Matcher matcher = customEventPattern.matcher(url);
        return matcher.matches();
    }
}
