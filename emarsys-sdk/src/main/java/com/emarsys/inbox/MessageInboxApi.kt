package com.emarsys.inbox

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.InboxResult

interface MessageInboxApi {
    fun fetchMessages(resultListener: ResultListener<Try<InboxResult>>)

    fun fetchMessages(resultListener: (Try<InboxResult>) -> Unit)

    fun addTag(tag: String, messageId: String)

    fun addTag(tag: String, messageId: String, completionListener: CompletionListener)

    fun addTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit)

    fun removeTag(tag: String, messageId: String)

    fun removeTag(tag: String, messageId: String, completionListener: CompletionListener)

    fun removeTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit)
}