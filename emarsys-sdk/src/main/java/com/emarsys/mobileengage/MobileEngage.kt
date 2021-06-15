package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.di.mobileEngage


class MobileEngage(private val loggingInstance: Boolean = false): MobileEngageApi {

    override fun setContact(contactFieldValue: String?, completionListener: CompletionListener?) {
        (if (loggingInstance) mobileEngage().loggingMobileEngageInternal else mobileEngage().mobileEngageInternal)
                .setContact(contactFieldValue, completionListener)
    }

    override fun setAuthenticatedContact(openIdToken: String, completionListener: CompletionListener?) {
        (if (loggingInstance) mobileEngage().loggingMobileEngageInternal else mobileEngage().mobileEngageInternal)
                .setAuthenticatedContact(openIdToken, completionListener)
    }

    override fun clearContact(completionListener: CompletionListener?) {
        (if (loggingInstance) mobileEngage().loggingMobileEngageInternal else mobileEngage().mobileEngageInternal)
                .clearContact(completionListener)
    }
}