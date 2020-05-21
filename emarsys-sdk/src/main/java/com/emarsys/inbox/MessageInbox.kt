package com.emarsys.inbox

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.di.Container.getDependency
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.inbox.MessageInboxInternal

@Mockable
class MessageInbox(private val loggingInstance: Boolean = false) : MessageInboxApi {
    override fun fetchMessages(resultListener: ResultListener<Try<InboxResult>>) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MessageInboxInternal>("defaultInstance"))
                .fetchMessages(resultListener)
    }

    override fun fetchMessages(resultListener: (Try<InboxResult>) -> Unit) {
        val javaResultListener = ResultListener<Try<InboxResult>> { resultListener.invoke(it) }
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MessageInboxInternal>("defaultInstance"))
                .fetchMessages(javaResultListener)
    }

    override fun addTag(tag: String, messageId: String) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MessageInboxInternal>("defaultInstance"))
                .addTag(tag, messageId, null)
    }

    override fun addTag(tag: String, messageId: String, completionListener: CompletionListener) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MessageInboxInternal>("defaultInstance"))
                .addTag(tag, messageId, completionListener)
    }

    override fun addTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit) {
        val javaCompletionListener = CompletionListener { completionListener.invoke(it) }
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MessageInboxInternal>("defaultInstance"))
                .addTag(tag, messageId, javaCompletionListener)
    }

    override fun removeTag(tag: String, messageId: String) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MessageInboxInternal>("defaultInstance"))
                .removeTag(tag, messageId, null)
    }

    override fun removeTag(tag: String, messageId: String, completionListener: CompletionListener) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MessageInboxInternal>("defaultInstance"))
                .removeTag(tag, messageId, completionListener)
    }

    override fun removeTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit) {
        val javaCompletionListener = CompletionListener { completionListener.invoke(it) }
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<MessageInboxInternal>("defaultInstance"))
                .removeTag(tag, messageId, javaCompletionListener)
    }
}