package com.emarsys.mobileengage.responsehandler

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.model.specification.FilterByCampaignId
import com.emarsys.mobileengage.util.RequestModelHelper

class InAppCleanUpResponseHandlerV4(
        private val displayedIamRepository: Repository<DisplayedIam, SqlSpecification>,
        private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>,
        private val requestModelHelper: RequestModelHelper) : AbstractResponseHandler() {

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        val requestModel = responseModel.requestModel
        return FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4) &&
                requestModelHelper.isCustomEvent(responseModel.requestModel) &&
                responseModel.statusCode in 200..299 &&
                (hasObject(requestModel, "viewedMessages") ||
                        hasObject(requestModel, "clicks"))
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val payload = responseModel.requestModel.payload
        if (payload != null && payload.containsKey("clicks")) {
            val campaignIdsToRemove: Array<String> = collectCampaignIds(payload["clicks"] as List<Map<String, Any?>>)
            if (campaignIdsToRemove.isNotEmpty()) {
                buttonClickedRepository.remove(FilterByCampaignId(*campaignIdsToRemove))
            }
        }
        if (payload != null && payload.containsKey("viewedMessages")) {
            val campaignIdsToRemove: Array<String> = collectCampaignIds(payload["viewedMessages"] as List<Map<String, Any?>>)
            if (campaignIdsToRemove.isNotEmpty()) {
                displayedIamRepository.remove(FilterByCampaignId(*campaignIdsToRemove))
            }
        }
    }

    private fun collectCampaignIds(items: List<Map<String, Any?>>): Array<String> {
        return items.map { buttonClick ->
            buttonClick["campaignId"] as String
        }.toTypedArray()
    }

    private fun hasObject(requestModel: RequestModel, key: String): Boolean {
        val payload = requestModel.payload
        return !payload.isNullOrEmpty() && payload.containsKey(key)
    }
}