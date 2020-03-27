package com.emarsys.mobileengage.inbox

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.InboxResult


interface MessageInboxInternal {
    fun fetchMessages(resultListener: ResultListener<Try<InboxResult>>)

    fun addTag(tag: String, messageId: String, completionListener: CompletionListener?)

    fun removeTag(tag: String, messageId: String, completionListener: CompletionListener?)
}