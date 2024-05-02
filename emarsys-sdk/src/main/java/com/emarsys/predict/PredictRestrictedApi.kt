package com.emarsys.predict

import com.emarsys.core.api.result.CompletionListener

interface PredictRestrictedApi {

    fun setContact(contactFieldId: Int, contactFieldValue: String, completionListener: CompletionListener?)

    fun clearPredictOnlyContact(completionListener: CompletionListener?)

    fun clearVisitorId()
}