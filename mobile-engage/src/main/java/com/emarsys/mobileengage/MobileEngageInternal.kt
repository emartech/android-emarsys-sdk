package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener

interface MobileEngageInternal {
    fun setContact(
        contactFieldId: Int?,
        contactFieldValue: String?,
        completionListener: CompletionListener?
    )
    fun setAuthenticatedContact(
        contactFieldId: Int,
        openIdToken: String,
        completionListener: CompletionListener?
    )
    fun clearContact(completionListener: CompletionListener?)
}