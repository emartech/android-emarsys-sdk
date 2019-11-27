package com.emarsys.mobileengage.endpoint;

import com.emarsys.mobileengage.BuildConfig;

public class Endpoint {
    public static final String ME_BASE_V2 = BuildConfig.ME_BASE_V2_URL;

    public static final String INBOX_BASE = BuildConfig.INBOX_BASE_URL;
    public static final String INBOX_RESET_BADGE_COUNT_V1 = INBOX_BASE + "reset-badge-count";
    public static final String INBOX_FETCH_V1 = INBOX_BASE + "notifications";

    public static final String DEEP_LINK_BASE = BuildConfig.DEEP_LINK_BASE_URL;
    public static final String DEEP_LINK_CLICK = DEEP_LINK_BASE + "clicks";

    public static final String ME_V3_CLIENT_HOST = "https://me-client.eservice.emarsys.net";
    public static final String ME_V3_CLIENT_BASE = ME_V3_CLIENT_HOST + "/v3/apps/%s/client";

    public static final String ME_V3_EVENT_HOST = "https://mobile-events.eservice.emarsys.net";
    public static final String ME_V3_EVENT_BASE = ME_V3_EVENT_HOST + "/v3/apps/%s/client/events";
}
