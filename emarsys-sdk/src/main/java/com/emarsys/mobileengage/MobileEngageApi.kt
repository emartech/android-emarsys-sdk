package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener

interface MobileEngageApi {

    fun setContact(
        contactFieldId: Int? = null,
        contactFieldValue: String? = null,
        completionListener: CompletionListener? = null
    )

    fun setAuthenticatedContact(
        contactFieldId: Int,
        openIdToken: String,
        completionListener: CompletionListener?
    )

    fun clearContact(completionListener: CompletionListener?)
}