package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.getDependency


class MobileEngage(private val loggingInstance: Boolean = false): MobileEngageApi {

    override fun setContact(contactFieldValue: String?, completionListener: CompletionListener?) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MobileEngageInternal>("defaultInstance"))
                .setContact(contactFieldValue, completionListener)
    }

    override fun setAuthenticatedContact(openIdToken: String, completionListener: CompletionListener?) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MobileEngageInternal>("defaultInstance"))
                .setAuthenticatedContact(openIdToken, completionListener)
    }

    override fun clearContact(completionListener: CompletionListener?) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MobileEngageInternal>("defaultInstance"))
                .clearContact(completionListener)
    }
}