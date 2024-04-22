package com.emarsys.mapper

import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.request.mapper.AbstractRequestMapper
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.predict.request.PredictRequestContext

class MerchantIdHeaderMapper(
    override val requestContext: MobileEngageRequestContext,
    override val requestModelHelper: RequestModelHelper,
    private val predictRequestContext: PredictRequestContext
) : AbstractRequestMapper(requestContext, requestModelHelper) {

    companion object {
        const val MERCHANT_ID_HEADER = "X-Merchant-Id"
    }

    override fun createHeaders(requestModel: RequestModel): Map<String, String> {
        val headers: MutableMap<String, String> = requestModel.headers.toMutableMap()
        if (!predictRequestContext.merchantId.isNullOrBlank()) {
            headers[MERCHANT_ID_HEADER] = predictRequestContext.merchantId!!
        }
        return headers
    }

    override fun shouldMapRequestModel(requestModel: RequestModel): Boolean {
        return (requestModelHelper.isMobileEngageSetContactRequest(requestModel)
                || requestModelHelper.isRefreshContactTokenRequest(requestModel)
                || requestModelHelper.isPredictMultiIdContactRequest(requestModel))
                && !predictRequestContext.merchantId.isNullOrBlank()
    }
}