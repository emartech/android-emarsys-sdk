package com.emarsys.predict.request

import com.emarsys.core.Mockable
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.predict.endpoint.Endpoint

@Mockable
class PredictMultiIdRequestModelFactory(
    private val requestContext: PredictRequestContext,
    private val clientServiceEndpointProvider: ServiceEndpointProvider,
) {

    fun createSetContactRequestModel(contactFieldId: Int, contactFieldValue: String): RequestModel {
        validateMerchantId()
        return RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
            .url(
                "${clientServiceEndpointProvider.provideEndpointHost()}${Endpoint.CLIENT_MULTI_ID_BASE}/contact-token"
            )
            .method(RequestMethod.POST)
            .payload(
                mapOf(
                    "contactFieldId" to contactFieldId,
                    "contactFieldValue" to contactFieldValue
                )
            )
            .build()
    }

    private fun validateMerchantId() {
        if (requestContext.merchantId.isNullOrBlank()) {
            throw IllegalArgumentException("Merchant Id must not be null!")
        }
    }
}