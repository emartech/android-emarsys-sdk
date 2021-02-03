package com.emarsys.mobileengage.responsehandler

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.response.AbstractResponseHandler
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.model.specification.FilterByCampaignId
import com.emarsys.mobileengage.util.RequestModelUtils.isCustomEvent

class InAppCleanUpResponseHandler(
        private val displayedIamRepository: Repository<DisplayedIam, SqlSpecification>,
        private val buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>,
        private val eventServiceProvider: ServiceEndpointProvider,
        private val eventServiceV4Provider: ServiceEndpointProvider) : AbstractResponseHandler() {

    companion object {
        private const val OLD_MESSAGES = "oldCampaigns"
    }

    override fun shouldHandleResponse(responseModel: ResponseModel): Boolean {
        var shouldHandle = false
        if (!FeatureRegistry.isFeatureEnabled(InnerFeature.EVENT_SERVICE_V4)) {
            val json = responseModel.parsedBody
            if (json != null && json.has(OLD_MESSAGES) && isCustomEventResponseModel(responseModel)) {
                val array = json.optJSONArray(OLD_MESSAGES)
                if (array != null) {
                    shouldHandle = array.length() > 0
                }
            }
        }
        return shouldHandle
    }

    private fun isCustomEventResponseModel(responseModel: ResponseModel): Boolean {
        return isCustomEvent(responseModel.requestModel, eventServiceProvider, eventServiceV4Provider)
    }

    override fun handleResponse(responseModel: ResponseModel) {
        val json = responseModel.parsedBody
        val oldMessages = json.optJSONArray(OLD_MESSAGES)
        if (oldMessages != null) {
            val ids = arrayOfNulls<String>(oldMessages.length())
            for (i in 0 until oldMessages.length()) {
                ids[i] = oldMessages.optString(i)
            }
            displayedIamRepository.remove(FilterByCampaignId(*ids))
            buttonClickedRepository.remove(FilterByCampaignId(*ids))
        }
    }
}