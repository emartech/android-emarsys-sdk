package com.emarsys.mobileengage.api.experimental;

import com.emarsys.core.api.experimental.FlipperFeature;

import java.util.Locale;

public enum MobileEngageFeature implements FlipperFeature {

    USER_CENTRIC_INBOX;

    @Override
    public String getName() {
        return name().toLowerCase(Locale.US);
    }

}