package com.emarsys.inbox

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.di.mobileEngage

@Mockable
class MessageInbox(private val loggingInstance: Boolean = false) : MessageInboxApi {
    override fun fetchMessages(resultListener: ResultListener<Try<InboxResult>>) {
        (if (loggingInstance) mobileEngage().loggingMessageInboxInternal else mobileEngage().messageInboxInternal)
                .fetchMessages(resultListener)
    }

    override fun fetchMessages(resultListener: (Try<InboxResult>) -> Unit) {
        val javaResultListener = ResultListener<Try<InboxResult>> { resultListener.invoke(it) }
        (if (loggingInstance) mobileEngage().loggingMessageInboxInternal else mobileEngage().messageInboxInternal)
                .fetchMessages(javaResultListener)
    }

    override fun addTag(tag: String, messageId: String) {
        (if (loggingInstance) mobileEngage().loggingMessageInboxInternal else mobileEngage().messageInboxInternal)
                .addTag(tag, messageId, null)
    }

    override fun addTag(tag: String, messageId: String, completionListener: CompletionListener) {
        (if (loggingInstance) mobileEngage().loggingMessageInboxInternal else mobileEngage().messageInboxInternal)
                .addTag(tag, messageId, completionListener)
    }

    override fun addTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit) {
        val javaCompletionListener = CompletionListener { completionListener.invoke(it) }
        (if (loggingInstance) mobileEngage().loggingMessageInboxInternal else mobileEngage().messageInboxInternal)
                .addTag(tag, messageId, javaCompletionListener)
    }

    override fun removeTag(tag: String, messageId: String) {
        (if (loggingInstance) mobileEngage().loggingMessageInboxInternal else mobileEngage().messageInboxInternal)
                .removeTag(tag, messageId, null)
    }

    override fun removeTag(tag: String, messageId: String, completionListener: CompletionListener) {
        (if (loggingInstance) mobileEngage().loggingMessageInboxInternal else mobileEngage().messageInboxInternal)
                .removeTag(tag, messageId, completionListener)
    }

    override fun removeTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit) {
        val javaCompletionListener = CompletionListener { completionListener.invoke(it) }
        (if (loggingInstance) mobileEngage().loggingMessageInboxInternal else mobileEngage().messageInboxInternal)
                .removeTag(tag, messageId, javaCompletionListener)
    }
}