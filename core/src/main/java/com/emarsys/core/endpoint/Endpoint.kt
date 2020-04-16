package com.emarsys.core.endpoint

object Endpoint {
    @JvmField
    val LOG_URL = "https://log-dealer.eservice.emarsys.net/v1/log"

    @JvmStatic
    val REMOTE_CONFIG_URL = "https://mobile-sdk-config.eservice.emarsys.net"

    fun remoteConfigUrl(applicationCode: String?): String {
        return "$REMOTE_CONFIG_URL/$applicationCode"
    }

    fun remoteConfigSignatureUrl(applicationCode: String?): String {
        return "$REMOTE_CONFIG_URL/signature/$applicationCode"
    }
}