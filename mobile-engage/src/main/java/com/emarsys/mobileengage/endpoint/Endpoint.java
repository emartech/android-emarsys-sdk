package com.emarsys.mobileengage.endpoint;

import com.emarsys.mobileengage.BuildConfig;

public class Endpoint {
    public static final String ME_BASE_V2 = BuildConfig.ME_BASE_V2_URL;
    public static final String ME_LOGIN_V2 = ME_BASE_V2 + "users/login";
    public static final String ME_LOGOUT_V2 = ME_BASE_V2 + "users/logout";
    public static final String ME_LAST_MOBILE_ACTIVITY_V2 = ME_BASE_V2 + "events/ems_lastMobileActivity";

    public static final String ME_BASE_V3 = BuildConfig.ME_BASE_V3_URL;

    public static final String INBOX_BASE = BuildConfig.INBOX_BASE_URL;
    public static final String INBOX_RESET_BADGE_COUNT_V1 = INBOX_BASE + "reset-badge-count";
    public static final String INBOX_FETCH_V1 = INBOX_BASE + "notifications";

    public static final String INBOX_RESET_BADGE_COUNT_V2 = INBOX_BASE + "v1/notifications/%s/count";
    public static final String INBOX_FETCH_V2 = INBOX_BASE + "v1/notifications/%s";

    public static final String DEEP_LINK_BASE = BuildConfig.DEEP_LINK_BASE_URL;
    public static final String DEEP_LINK_CLICK = DEEP_LINK_BASE + "clicks";
}
