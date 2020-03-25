package com.emarsys.inbox

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.MessageInboxResult

interface MessageInboxApi {
    fun fetchNotifications(resultListener: ResultListener<Try<MessageInboxResult>>)

    fun fetchNotifications(resultListener: (Try<MessageInboxResult>) -> Unit)

    fun addTag(tag: String, messageId: String)

    fun addTag(tag: String, messageId: String, completionListener: CompletionListener)

    fun addTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit)

    fun removeTag(tag: String, messageId: String)

    fun removeTag(tag: String, messageId: String, completionListener: CompletionListener)

    fun removeTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit)
}