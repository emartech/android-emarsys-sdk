package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.di.mobileEngage


class MobileEngage(private val loggingInstance: Boolean = false): MobileEngageApi {

    override fun setContact(
        contactFieldId: Int?,
        contactFieldValue: String?,
        completionListener: CompletionListener?
    ) {
        (if (loggingInstance) mobileEngage().loggingMobileEngageInternal else mobileEngage().mobileEngageInternal)
                .setContact(contactFieldId, contactFieldValue, completionListener)
    }

    override fun setAuthenticatedContact(
        contactFieldId: Int,
        openIdToken: String,
        completionListener: CompletionListener?
    ) {
        (if (loggingInstance) mobileEngage().loggingMobileEngageInternal else mobileEngage().mobileEngageInternal)
                .setAuthenticatedContact(contactFieldId, openIdToken, completionListener)
    }

    override fun clearContact(completionListener: CompletionListener?) {
        (if (loggingInstance) mobileEngage().loggingMobileEngageInternal else mobileEngage().mobileEngageInternal)
                .clearContact(completionListener)
    }
}