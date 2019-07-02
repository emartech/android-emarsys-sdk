package com.emarsys.feature;

import com.emarsys.core.api.experimental.FlipperFeature;

public enum InnerFeature implements FlipperFeature {
    MOBILE_ENGAGE, PREDICT;

    @Override
    public String getName() {
        return "inner_feature_" + name().toLowerCase();
    }
}
