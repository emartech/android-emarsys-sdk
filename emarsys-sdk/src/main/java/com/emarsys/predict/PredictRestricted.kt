package com.emarsys.predict

import com.emarsys.core.Mockable
import com.emarsys.core.di.getDependency

@Mockable
class PredictRestricted(private val loggingInstance: Boolean = false) : PredictRestrictedApi {
    override fun setContact(contactId: String) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .setContact(contactId)
    }

    override fun clearContact() {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .clearContact()
    }
}