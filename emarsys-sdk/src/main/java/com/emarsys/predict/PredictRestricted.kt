package com.emarsys.predict

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.predict.di.predict

@Mockable
class PredictRestricted(private val loggingInstance: Boolean = false) : PredictRestrictedApi {
    override fun setContact(contactFieldId: Int, contactFieldValue: String, completionListener: CompletionListener?) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
            .setContact(contactFieldId, contactFieldValue, completionListener)
    }

    override fun clearPredictOnlyContact(completionListener: CompletionListener?) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
            .clearPredictOnlyContact(completionListener)
    }
}