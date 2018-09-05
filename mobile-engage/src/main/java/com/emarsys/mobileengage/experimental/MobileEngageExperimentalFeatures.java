package com.emarsys.mobileengage.experimental;

import com.emarsys.core.experimental.ExperimentalFeatures;
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature;


public class MobileEngageExperimentalFeatures {

    public static boolean isV3Enabled() {
        return ExperimentalFeatures.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)
                || ExperimentalFeatures.isFeatureEnabled(MobileEngageFeature.USER_CENTRIC_INBOX);
    }

}
