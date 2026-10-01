package com.emarsys.core.util

import androidx.core.net.toUri
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestModel

object RequestModelUtils {

    fun extractIdsFromCompositeRequestModel(requestModel: RequestModel): List<String> {
        return if (requestModel is CompositeRequestModel) {
            requestModel.originalRequestIds.toList()
        } else {
            listOf(requestModel.id)
        }
    }

    fun extractQueryParameters(requestModel: RequestModel): Map<String, String> {
        val uri = requestModel.url.toString().toUri()
        val queryParameterNames = uri.queryParameterNames
        return if (queryParameterNames.isEmpty()) {
            emptyMap()
        } else {
            queryParameterNames.associateWith { uri.getQueryParameter(it) ?: "" }
        }
    }
}
