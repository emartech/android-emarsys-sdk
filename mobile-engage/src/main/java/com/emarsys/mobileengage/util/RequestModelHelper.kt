package com.emarsys.mobileengage.util

import com.emarsys.core.Mockable
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestModel

@Mockable
class RequestModelHelper(
    private val clientServiceEndpointProvider: ServiceEndpointProvider,
    private val eventServiceEndpointProvider: ServiceEndpointProvider,
    private val messageInboxServiceEndpointProvider: ServiceEndpointProvider,
    private val predictServiceEndpointProvider: ServiceEndpointProvider
) {

    companion object {
        const val CONTACT_FIELD_ID_KEY = "contactFieldId"
        const val CONTACT_FIELD_VALUE_KEY = "contactFieldValue"
        const val REFRESH_TOKEN_KEY = "refreshToken"
    }

    fun isMobileEngageRequest(requestModel: RequestModel): Boolean {
        val clientServiceUrl = clientServiceEndpointProvider.provideEndpointHost()
        val eventServiceUrl = eventServiceEndpointProvider.provideEndpointHost()
        val messageInboxServiceUrl = messageInboxServiceEndpointProvider.provideEndpointHost()

        val url = requestModel.url.toString()
        return url.startsWithOneOf(clientServiceUrl, eventServiceUrl, messageInboxServiceUrl)
    }

    fun isRemoteConfigRequest(requestModel: RequestModel): Boolean {
        val url = requestModel.url.toString()

        return url.startsWith(com.emarsys.core.endpoint.Endpoint.REMOTE_CONFIG_URL)
    }

    fun isCustomEvent(requestModel: RequestModel): Boolean {
        val eventServiceUrl = eventServiceEndpointProvider.provideEndpointHost()

        val url = requestModel.url.toString()
        return url.startsWithOneOf(eventServiceUrl) && url.endsWith("/events")
    }

    fun isInlineInAppRequest(requestModel: RequestModel): Boolean {
        val eventServiceUrl = eventServiceEndpointProvider.provideEndpointHost()

        val url = requestModel.url.toString()
        return url.startsWithOneOf(eventServiceUrl) && url.endsWith("/inline-messages")
    }

    fun isRefreshContactTokenRequest(requestModel: RequestModel): Boolean {
        val clientServiceUrl = clientServiceEndpointProvider.provideEndpointHost()

        val url = requestModel.url.toString()
        return url.startsWithOneOf(clientServiceUrl) && url.endsWith("/client/contact-token")
    }

    fun isPredictMultiIdContactRequest(requestModel: RequestModel): Boolean {
        val clientServiceUrl = clientServiceEndpointProvider.provideEndpointHost()

        val url = requestModel.url.toString()
        return url.startsWith(clientServiceUrl) && !url.contains("apps") && url.endsWith("/contact-token")
    }

    fun isPredictMultiIdSetContactRequest(requestModel: RequestModel): Boolean {
        val clientServiceUrl = clientServiceEndpointProvider.provideEndpointHost()

        val url = requestModel.url.toString()
        return url.startsWith(clientServiceUrl) && !url.contains("apps") && url.endsWith("/contact-token")
                && (requestModel.payload?.containsKey(CONTACT_FIELD_ID_KEY) ?: false)
                && (requestModel.payload?.containsKey(CONTACT_FIELD_VALUE_KEY) ?: false)
    }

    fun isPredictMultiIdRefreshContactTokenRequest(requestModel: RequestModel): Boolean {
        val clientServiceUrl = clientServiceEndpointProvider.provideEndpointHost()

        val url = requestModel.url.toString()
        return url.startsWith(clientServiceUrl) && !url.contains("apps") && url.endsWith("/contact-token")
                && (requestModel.payload?.containsKey(REFRESH_TOKEN_KEY) ?: false)
    }

    fun isMobileEngageSetContactRequest(requestModel: RequestModel): Boolean {
        val clientServiceUrl = clientServiceEndpointProvider.provideEndpointHost()

        val url = requestModel.url.toString()
        return url.startsWithOneOf(clientServiceUrl) && url.endsWith("client/contact")
    }

    fun isPredictRequest(requestModel: RequestModel): Boolean {
        val predictServiceUrl = predictServiceEndpointProvider.provideEndpointHost()

        val url = requestModel.url.toString()
        return url.startsWith(predictServiceUrl)
    }

    private fun String.startsWithOneOf(vararg patterns: String): Boolean {
        return patterns.any { this.startsWith(it) }
    }

}