package com.emarsys.mobileengage.endpoint;

import com.emarsys.mobileengage.BuildConfig;

public class Endpoint {
    public static final String ME_BASE_V2 = BuildConfig.ME_BASE_V2_URL;
    public static final String INBOX_BASE = BuildConfig.INBOX_BASE_URL;
    public static final String DEEP_LINK = BuildConfig.DEEP_LINK_BASE_URL;

    public static final String ME_V3_CLIENT_HOST = "https://me-client.eservice.emarsys.net";

    public static final String ME_V3_EVENT_HOST = "https://mobile-events.eservice.emarsys.net";

    public static final String FETCH_GEOFENCES_URL = "https://ems-mobile-development.s3-eu-west-1.amazonaws.com/geofenceTest.json";

    public static String clientBase(String applicationCode) {
        return "/v3/apps/" + applicationCode + "/client";
    }

    public static String eventBase(String applicationCode) {
        return "/v3/apps/" + applicationCode + "/client/events";
    }
}
