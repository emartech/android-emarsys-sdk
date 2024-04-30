package com.emarsys.predict.request

import com.emarsys.core.Mockable
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.predict.endpoint.Endpoint

@Mockable
class PredictMultiIdRequestModelFactory(
    private val predictRequestContext: PredictRequestContext,
    private val clientServiceEndpointProvider: ServiceEndpointProvider,
) {

    fun createSetContactRequestModel(contactFieldId: Int, contactFieldValue: String): RequestModel {
        validateMerchantId()
        return RequestModel.Builder(predictRequestContext.timestampProvider, predictRequestContext.uuidProvider)
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

    fun createRefreshContactTokenRequestModel(refreshToken: String): RequestModel {
        validateMerchantId()
        return RequestModel.Builder(predictRequestContext.timestampProvider, predictRequestContext.uuidProvider)
            .url(
                "${clientServiceEndpointProvider.provideEndpointHost()}${Endpoint.CLIENT_MULTI_ID_BASE}/contact-token"
            )
            .method(RequestMethod.POST)
            .payload(
                mapOf(
                    "refreshToken" to refreshToken
                )
            )
            .build()
    }

    fun createClearContactRequestModel(): RequestModel {
        validateMerchantId()
        return RequestModel.Builder(predictRequestContext.timestampProvider, predictRequestContext.uuidProvider)
            .url(
                "${clientServiceEndpointProvider.provideEndpointHost()}${Endpoint.CLIENT_MULTI_ID_BASE}/contact-token"
            )
            .method(RequestMethod.DELETE)
            .build()
    }

    private fun validateMerchantId() {
        if (predictRequestContext.merchantId.isNullOrBlank()) {
            throw IllegalArgumentException("Merchant Id must not be null!")
        }
    }
}