package com.emarsys.mobileengage.request

import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.emarsys.mobileengage.util.RequestPayloadUtils
import com.emarsys.mobileengage.util.RequestPayloadUtils.createCustomEventPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createInternalCustomEventPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createRefreshContactTokenPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createSetPushTokenPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createTrackDeviceInfoPayload
import java.util.*

@Mockable
class MobileEngageRequestModelFactory(private val requestContext: MobileEngageRequestContext,
                                      private val clientServiceProvider: ServiceEndpointProvider,
                                      private val eventServiceProvider: ServiceEndpointProvider,
                                      private val messageInboxServiceProvider: ServiceEndpointProvider,
                                      private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>) {

    fun createSetPushTokenRequest(pushToken: String): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${clientServiceProvider.provideEndpointHost()}${Endpoint.clientBase(requestContext.applicationCode)}/push-token")
                .method(RequestMethod.PUT)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(createSetPushTokenPayload(pushToken))
                .build()
    }

    fun createRemovePushTokenRequest(): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${clientServiceProvider.provideEndpointHost()}${Endpoint.clientBase(requestContext.applicationCode)}/push-token")
                .method(RequestMethod.DELETE)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .build()
    }

    fun createTrackDeviceInfoRequest(): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${clientServiceProvider.provideEndpointHost()}${Endpoint.clientBase(requestContext.applicationCode)}")
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(createTrackDeviceInfoPayload(requestContext))
                .build()
    }

    fun createSetContactRequest(contactFieldId: Int?, contactFieldValue: String?): RequestModel {
        val builder = RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${clientServiceProvider.provideEndpointHost()}${Endpoint.clientBase(requestContext.applicationCode)}/contact")
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))

        if (!requestContext.hasContactIdentification()) {
            val queryParams: MutableMap<String, String> = HashMap()
            queryParams["anonymous"] = "true"
            builder.payload(emptyMap())
            builder.queryParams(queryParams)
        } else {
            val payload = mutableMapOf<String, Any>()
            if (contactFieldId != null) {
                payload["contactFieldId"] = contactFieldId
            }
            if (contactFieldValue != null) {
                payload["contactFieldValue"] = contactFieldValue
            }
            builder.payload(payload)
        }
        return builder.build()
    }

    fun createCustomEventRequest(eventName: String, eventAttributes: Map<String, String>?): RequestModel {
        val payload = createCustomEventPayload(eventName, eventAttributes, requestContext)
        return createEvent(payload, requestContext)
    }

    fun createInternalCustomEventRequest(eventName: String, eventAttributes: Map<String, String>?): RequestModel {
        val payload = createInternalCustomEventPayload(eventName, eventAttributes, requestContext)
        return createEvent(payload, requestContext)
    }

    fun createRefreshContactTokenRequest(): RequestModel {
        val headers: MutableMap<String, String> = HashMap()
        headers.putAll(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
        headers.putAll(RequestHeaderUtils.createDefaultHeaders(requestContext))
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${clientServiceProvider.provideEndpointHost()}${Endpoint.clientBase(requestContext.applicationCode)}/contact-token")
                .method(RequestMethod.POST)
                .headers(headers)
                .payload(createRefreshContactTokenPayload(requestContext))
                .build()
    }

    private fun createEvent(payload: Map<String, Any>, requestContext: MobileEngageRequestContext): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${eventServiceProvider.provideEndpointHost()}${Endpoint.eventBase(requestContext.applicationCode)}")
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(payload)
                .build()
    }

    fun createFetchInboxMessagesRequest(): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .method(RequestMethod.GET)
                .url("${messageInboxServiceProvider.provideEndpointHost()}${Endpoint.inboxBase(requestContext.applicationCode)}")
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .build()
    }

    fun createFetchGeofenceRequest(): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .method(RequestMethod.GET)
                .url("${clientServiceProvider.provideEndpointHost()}${Endpoint.geofencesBase(requestContext.applicationCode)}")
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .build()
    }

    fun createFetchInlineInAppMessagesRequest(viewId: String): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .method(RequestMethod.POST)
                .payload(RequestPayloadUtils.createInlineInAppPayload(viewId, buttonClickedRepository.query(Everything())))
                .url("${eventServiceProvider.provideEndpointHost()}${Endpoint.inlineInAppBase(requestContext.applicationCode)}")
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext) + RequestHeaderUtils.createDefaultHeaders(requestContext))
                .build()
    }
}