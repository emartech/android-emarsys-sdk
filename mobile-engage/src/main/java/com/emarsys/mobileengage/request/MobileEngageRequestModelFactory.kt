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
import com.emarsys.mobileengage.util.RequestPayloadUtils
import com.emarsys.mobileengage.util.RequestPayloadUtils.createCustomEventPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createInternalCustomEventPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createRefreshContactTokenPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createSetPushTokenPayload
import com.emarsys.mobileengage.util.RequestPayloadUtils.createTrackDeviceInfoPayload

@Mockable
class MobileEngageRequestModelFactory(
    private val requestContext: MobileEngageRequestContext,
    private val clientServiceProvider: ServiceEndpointProvider,
    private val eventServiceProvider: ServiceEndpointProvider,
    private val messageInboxServiceProvider: ServiceEndpointProvider,
    private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>
) {

    fun createSetPushTokenRequest(pushToken: String): RequestModel {
        val applicationCode = validateApplicationCode()
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
            .url(
                "${clientServiceProvider.provideEndpointHost()}${
                    Endpoint.clientBase(
                        applicationCode
                    )
                }/push-token"
            )
            .method(RequestMethod.PUT)
            .payload(createSetPushTokenPayload(pushToken))
            .build()
    }

    private fun validateApplicationCode(): String {
        return if (requestContext.applicationCode.isNullOrBlank()) {
            throw IllegalArgumentException(
                "Application Code must not be null!"
            )
        } else {
            requestContext.applicationCode!!
        }
    }

    fun createRemovePushTokenRequest(): RequestModel {
        val applicationCode = validateApplicationCode()

        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
            .url(
                "${clientServiceProvider.provideEndpointHost()}${
                    Endpoint.clientBase(
                        applicationCode
                    )
                }/push-token"
            )
            .method(RequestMethod.DELETE)
            .build()
    }

    fun createTrackDeviceInfoRequest(): RequestModel {
        val applicationCode = validateApplicationCode()

        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
            .url(
                "${clientServiceProvider.provideEndpointHost()}${
                    Endpoint.clientBase(
                        applicationCode
                    )
                }"
            )
            .method(RequestMethod.POST)
            .payload(createTrackDeviceInfoPayload(requestContext))
            .build()
    }

    fun createSetContactRequest(contactFieldId: Int?, contactFieldValue: String?): RequestModel {
        val applicationCode = validateApplicationCode()

        val builder =
            RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .url(
                    "${clientServiceProvider.provideEndpointHost()}${
                        Endpoint.clientBase(
                            applicationCode
                        )
                    }/contact"
                )
                .method(RequestMethod.POST)

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

    fun createCustomEventRequest(
        eventName: String,
        eventAttributes: Map<String, String>?
    ): RequestModel {
        val payload = createCustomEventPayload(eventName, eventAttributes, requestContext)
        return createEvent(payload, requestContext)
    }

    fun createInternalCustomEventRequest(
        eventName: String,
        eventAttributes: Map<String, String>?
    ): RequestModel {
        val payload = createInternalCustomEventPayload(eventName, eventAttributes, requestContext)
        return createEvent(payload, requestContext)
    }

    fun createRefreshContactTokenRequest(): RequestModel {
        val applicationCode = validateApplicationCode()

        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
            .url(
                "${clientServiceProvider.provideEndpointHost()}${
                    Endpoint.clientBase(
                        applicationCode
                    )
                }/contact-token"
            )
            .method(RequestMethod.POST)
            .payload(createRefreshContactTokenPayload(requestContext))
            .build()
    }

    private fun createEvent(
        payload: Map<String, Any>,
        requestContext: MobileEngageRequestContext
    ): RequestModel {
        val applicationCode = validateApplicationCode()

        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
            .url(
                "${eventServiceProvider.provideEndpointHost()}${
                    Endpoint.eventBase(
                        applicationCode
                    )
                }"
            )
            .method(RequestMethod.POST)
            .payload(payload)
            .build()
    }

    fun createFetchInboxMessagesRequest(): RequestModel {
        val applicationCode = validateApplicationCode()

        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
            .method(RequestMethod.GET)
            .url(
                "${messageInboxServiceProvider.provideEndpointHost()}${
                    Endpoint.inboxBase(
                        applicationCode
                    )
                }"
            )
            .build()
    }

    fun createFetchGeofenceRequest(): RequestModel {
        val applicationCode = validateApplicationCode()

        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
            .method(RequestMethod.GET)
            .url(
                "${clientServiceProvider.provideEndpointHost()}${
                    Endpoint.geofencesBase(
                        applicationCode
                    )
                }"
            )
            .build()
    }

    fun createFetchInlineInAppMessagesRequest(viewId: String): RequestModel {
        val applicationCode = validateApplicationCode()

        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
            .method(RequestMethod.POST)
            .payload(
                RequestPayloadUtils.createInlineInAppPayload(
                    viewId,
                    buttonClickedRepository.query(Everything())
                )
            )
            .url(
                "${eventServiceProvider.provideEndpointHost()}${
                    Endpoint.inlineInAppBase(
                        applicationCode
                    )
                }"
            )
            .build()
    }
}