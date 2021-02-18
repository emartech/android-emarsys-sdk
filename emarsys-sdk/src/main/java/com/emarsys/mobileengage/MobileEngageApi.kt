package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener

interface MobileEngageApi {

    fun setContact(contactFieldValue: String?, completionListener: CompletionListener?)

    fun setAuthorizedContact(contactFieldValue: String?, idToken: String, completionListener: CompletionListener?)

    fun clearContact(completionListener: CompletionListener?)
}