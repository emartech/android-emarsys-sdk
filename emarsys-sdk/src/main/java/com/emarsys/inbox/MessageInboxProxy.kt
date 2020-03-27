package com.emarsys.inbox

import com.emarsys.core.Mockable
import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.inbox.MessageInboxInternal

@Mockable
class MessageInboxProxy(private val runnerProxy: RunnerProxy, private val inboxInternal: MessageInboxInternal) : MessageInboxApi {
    override fun fetchMessages(resultListener: ResultListener<Try<InboxResult>>) {
        runnerProxy.logException {
            inboxInternal.fetchMessages(resultListener)
        }
    }

    override fun fetchMessages(resultListener: (Try<InboxResult>) -> Unit) {
        val javaResultListener = ResultListener<Try<InboxResult>> { resultListener.invoke(it) }
        fetchMessages(javaResultListener)
    }

    override fun addTag(tag: String, messageId: String) {
        runnerProxy.logException {
            inboxInternal.addTag(tag, messageId, null)
        }
    }

    override fun addTag(tag: String, messageId: String, completionListener: CompletionListener) {
        runnerProxy.logException {
            inboxInternal.addTag(tag, messageId, completionListener)
        }
    }

    override fun addTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit) {
        val javaCompletionListener = CompletionListener { completionListener.invoke(it) }
        runnerProxy.logException {
            inboxInternal.addTag(tag, messageId, javaCompletionListener)
        }
    }

    override fun removeTag(tag: String, messageId: String) {
        runnerProxy.logException {
            inboxInternal.removeTag(tag, messageId, null)
        }
    }

    override fun removeTag(tag: String, messageId: String, completionListener: CompletionListener) {
        runnerProxy.logException {
            inboxInternal.removeTag(tag, messageId, completionListener)
        }
    }

    override fun removeTag(tag: String, messageId: String, completionListener: (Throwable?) -> Unit) {
        val javaCompletionListener = CompletionListener { completionListener.invoke(it) }
        runnerProxy.logException {
            inboxInternal.removeTag(tag, messageId, javaCompletionListener)
        }
    }
}