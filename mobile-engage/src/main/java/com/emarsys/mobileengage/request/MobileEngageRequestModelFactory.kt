package com.emarsys.mobileengage.request

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.feature.FeatureRegistry
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
import com.emarsys.mobileengage.util.RequestPayloadUtils.createSetContactPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createSetPushTokenPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createTrackDeviceInfoPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createTrackNotificationOpenPayload
import java.util.*

@Mockable
class MobileEngageRequestModelFactory(private val requestContext: MobileEngageRequestContext,
                                      private val clientServiceProvider: ServiceEndpointProvider,
                                      private val eventServiceProvider: ServiceEndpointProvider,
                                      private val eventServiceV4Provider: ServiceEndpointProvider,
                                      private val mobileEngageV2Provider: ServiceEndpointProvider,
                                      private val inboxServiceProvider: ServiceEndpointProvider,
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

    fun createSetContactRequest(contactFieldValue: String?): RequestModel {
        val builder = RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${clientServiceProvider.provideEndpointHost()}${Endpoint.clientBase(requestContext.applicationCode)}/contact")
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
        if (contactFieldValue == null) {
            val queryParams: MutableMap<String, String> = HashMap()
            queryParams["anonymous"] = "true"
            builder.payload(emptyMap())
            builder.queryParams(queryParams)
        } else {
            builder.payload(createSetContactPayload(contactFieldValue, requestContext))
        }
        return builder.build()
    }

    fun createTrackNotificationOpenRequest(sid: String): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${mobileEngageV2Provider.provideEndpointHost()}events/message_open")
                .payload(createTrackNotificationOpenPayload(sid, requestContext))
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext))
                .build()
    }

    fun createResetBadgeCountRequest(): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${inboxServiceProvider.provideEndpointHost()}reset-badge-count")
                .headers(RequestHeaderUtils.createInboxHeaders(requestContext))
                .method(RequestMethod.POST)
                .build()
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
        val url = if (FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4)) {
            eventServiceV4Provider.provideEndpointHost()
        } else {
            eventServiceProvider.provideEndpointHost()
        }
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("$url${Endpoint.eventBase(requestContext.applicationCode)}")
                .method(RequestMethod.POST)
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .payload(payload)
                .build()
    }

    fun createFetchNotificationsRequest(): RequestModel {
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url("${inboxServiceProvider.provideEndpointHost()}notifications")
                .headers(RequestHeaderUtils.createInboxHeaders(requestContext))
                .method(RequestMethod.GET)
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
        val url = if (FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4)) {
            eventServiceV4Provider.provideEndpointHost()
        } else {
            eventServiceProvider.provideEndpointHost()
        }
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .method(RequestMethod.POST)
                .payload(RequestPayloadUtils.createInlineInAppPayload(viewId, buttonClickedRepository.query(Everything())))
                .url("$url${Endpoint.inlineInAppBase(requestContext.applicationCode)}")
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext) + RequestHeaderUtils.createDefaultHeaders(requestContext))
                .build()
    }
}