package com.emarsys.core.endpoint

object Endpoint {
    const val LOG_URL = "https://log-dealer.eservice.emarsys.net/v1/log"

    const val REMOTE_CONFIG_URL = "https://mobile-sdk-config.eservice.emarsys.net"

    fun remoteConfigUrl(applicationCode: String?): String {
        return "$REMOTE_CONFIG_URL/$applicationCode"
    }

    fun remoteConfigSignatureUrl(applicationCode: String?): String {
        return "$REMOTE_CONFIG_URL/signature/$applicationCode"
    }
}