package com.emarsys.mobileengage.endpoint

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.feature.FeatureRegistry.isFeatureEnabled

object Endpoint {
    const val DEEP_LINK = "https://deep-link.eservice.emarsys.net"
    const val ME_CLIENT_HOST = "https://me-client.eservice.emarsys.net"
    const val ME_EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
    const val ME_V3_INBOX_HOST = "https://me-inbox.eservice.emarsys.net/v3"
    fun deepLinkBase(): String {
        return "/api/clicks"
    }

    fun clientBase(applicationCode: String): String {
        return "/v3/apps/$applicationCode/client"
    }

    fun eventBase(applicationCode: String): String {
        var version = "v3"
        if (isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4)) {
            version = "v4"
        }
        return "/$version/apps/$applicationCode/client/events"
    }

    fun inboxBase(applicationCode: String): String {
        return "/apps/$applicationCode/inbox"
    }

    fun geofencesBase(applicationCode: String): String {
        return "/v3/apps/$applicationCode/geo-fences"
    }

    fun inlineInAppBase(applicationCode: String): String {
        var version = "v3"
        if (isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4)) {
            version = "v4"
        }
        return "/$version/apps/$applicationCode/inline-messages"
    }
}