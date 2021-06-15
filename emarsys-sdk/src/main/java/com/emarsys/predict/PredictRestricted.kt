package com.emarsys.predict

import com.emarsys.core.Mockable
import com.emarsys.predict.di.predict

@Mockable
class PredictRestricted(private val loggingInstance: Boolean = false) : PredictRestrictedApi {
    override fun setContact(contactId: String) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .setContact(contactId)
    }

    override fun clearContact() {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .clearContact()
    }
}