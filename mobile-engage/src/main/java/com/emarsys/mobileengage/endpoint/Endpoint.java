package com.emarsys.mobileengage.endpoint;

import com.emarsys.common.feature.InnerFeature;
import com.emarsys.core.feature.FeatureRegistry;
import com.emarsys.mobileengage.BuildConfig;

public class Endpoint {
    public static final String DEEP_LINK = "https://deep-link.eservice.emarsys.net";

    public static final String ME_CLIENT_HOST = "https://me-client.eservice.emarsys.net";

    public static final String ME_EVENT_HOST = "https://mobile-events.eservice.emarsys.net";

    public static final String ME_V3_INBOX_HOST = "https://me-inbox.eservice.emarsys.net/v3";

    public static String deepLinkBase(){
        return "/api/clicks";
    }

    public static String clientBase(String applicationCode) {
        return "/v3/apps/" + applicationCode + "/client";
    }

    public static String eventBase(String applicationCode) {
        String version = "v3";
        if (FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4)) {
            version = "v4";
        }
        return "/" + version + "/apps/" + applicationCode + "/client/events";
    }

    public static String inboxBase(String applicationCode) {
        return "/apps/" + applicationCode + "/inbox";
    }

    public static String geofencesBase(String applicationCode) {
        return "/v3/apps/" + applicationCode + "/geo-fences";
    }

    public static String inlineInAppBase(String applicationCode) {
        String version = "v3";
        if (FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4)) {
            version = "v4";
        }
        return "/" + version + "/apps/" + applicationCode + "/inline-messages";
    }
}
