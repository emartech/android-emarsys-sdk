package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener

interface MobileEngageApi {

    fun setContact(contactFieldValue: String?, completionListener: CompletionListener?)

    fun setAuthenticatedContact(openIdToken: String, completionListener: CompletionListener?)

    fun clearContact(completionListener: CompletionListener?)
}