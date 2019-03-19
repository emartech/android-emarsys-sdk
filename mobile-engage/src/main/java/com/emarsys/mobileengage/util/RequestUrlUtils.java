package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.endpoint.Endpoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUrlUtils {
    private static Pattern customEventPattern = Pattern.compile("https:\\/\\/mobile-events\\.eservice\\.emarsys\\.net(.+)\\/events");

    public static String createSetPushTokenUrl(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE + "/push-token", requestContext.getApplicationCode());
    }

    public static String createTrackDeviceInfoUrl(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE, requestContext.getApplicationCode());
    }

    public static String createSetContactUrl(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE + "/contact", requestContext.getApplicationCode());
    }

    public static String createCustomEventUrl(RequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_EVENT_BASE, requestContext.getApplicationCode());
    }

    public static boolean isMobileEngageUrl(String url) {
        Assert.notNull(url, "Url must not be null!");

        return url.startsWith(Endpoint.ME_V3_CLIENT_HOST) || url.startsWith(Endpoint.ME_V3_EVENT_HOST);
    }

    public static boolean isCustomEvent_V3(String url) {
        Assert.notNull(url, "Url must not be null");
        Matcher matcher = customEventPattern.matcher(url);
        return matcher.matches();
    }
}
