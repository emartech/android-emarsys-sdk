package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageRequestContext;
import com.emarsys.mobileengage.endpoint.Endpoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.emarsys.mobileengage.endpoint.Endpoint.ME_BASE_V2;

public class RequestUrlUtils {
    private static Pattern customEventPattern = Pattern.compile("https://mobile-events\\.eservice\\.emarsys\\.net/(.+)/events");
    private static Pattern refreshContactTokenPattern = Pattern.compile("https://me-client\\.eservice\\.emarsys\\.net/(.+)/contact-token");

    public static String createSetPushTokenUrl(MobileEngageRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE + "/push-token", requestContext.getApplicationCode());
    }

    public static String createRemovePushTokenUrl(MobileEngageRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE + "/push-token", requestContext.getApplicationCode());
    }

    public static String createTrackDeviceInfoUrl(MobileEngageRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE, requestContext.getApplicationCode());
    }

    public static String createSetContactUrl(MobileEngageRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE + "/contact", requestContext.getApplicationCode());
    }

    public static String createCustomEventUrl(MobileEngageRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_EVENT_BASE, requestContext.getApplicationCode());
    }

    public static String createRefreshContactTokenUrl(MobileEngageRequestContext requestContext) {
        Assert.notNull(requestContext, "RequestContext must not be null!");

        return String.format(Endpoint.ME_V3_CLIENT_BASE + "/contact-token", requestContext.getApplicationCode());
    }

    public static String createEventUrl_V2(String eventName) {
        Assert.notNull(eventName, "EventName must not be null!");
        return ME_BASE_V2 + "events/" + eventName;
    }

    public static boolean isMobileEngageV3Url(String url) {
        Assert.notNull(url, "Url must not be null!");

        return url.startsWith(Endpoint.ME_V3_CLIENT_HOST) || url.startsWith(Endpoint.ME_V3_EVENT_HOST);
    }

    public static boolean isCustomEvent_V3(String url) {
        Assert.notNull(url, "Url must not be null!");

        Matcher matcher = customEventPattern.matcher(url);

        return matcher.matches();
    }

    public static boolean isRefreshContactTokenUrl(String url) {
        Assert.notNull(url, "Url must not be null!");

        Matcher matcher = refreshContactTokenPattern.matcher(url);

        return matcher.matches();
    }
}
