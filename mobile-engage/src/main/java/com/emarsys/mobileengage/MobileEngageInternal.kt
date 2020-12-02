package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener

interface MobileEngageInternal {
    fun setContact(contactFieldValue: String?, completionListener: CompletionListener?)
    fun clearContact(completionListener: CompletionListener?)
}