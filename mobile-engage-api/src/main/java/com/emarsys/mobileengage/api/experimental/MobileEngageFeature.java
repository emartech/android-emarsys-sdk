package com.emarsys.mobileengage.api.experimental;

import com.emarsys.core.api.experimental.FlipperFeature;

import java.util.Locale;

public enum MobileEngageFeature implements FlipperFeature {

    IN_APP_MESSAGING, USER_CENTRIC_INBOX, TRACK_MESSAGE_OPEN_V3;

    @Override
    public String getName() {
        return name().toLowerCase(Locale.US);
    }

}