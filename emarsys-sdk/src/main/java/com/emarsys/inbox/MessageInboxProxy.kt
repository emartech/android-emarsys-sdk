package com.emarsys.inbox

import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.InboxMessage
import com.emarsys.mobileengage.api.inbox.MessageInboxResult
import com.emarsys.mobileengage.inbox.MessageInboxInternal


class MessageInboxProxy(private val runnerProxy: RunnerProxy, private val inboxInternal: MessageInboxInternal) : MessageInboxApi {
    override fun fetchNotifications(resultListener: ResultListener<Try<MessageInboxResult>>) {
        runnerProxy.logException {
            inboxInternal.fetchInboxMessages(resultListener)
        }
    }

    override fun fetchNotifications(resultListener: (Try<MessageInboxResult>) -> Unit) {
        val javaResultListener = ResultListener<Try<MessageInboxResult>> { resultListener.invoke(it) }
        fetchNotifications(javaResultListener)
    }

    override fun trackNotificationOpen(inboxMessage: InboxMessage) {
        runnerProxy.logException {
            inboxInternal.trackNotificationOpen(inboxMessage, null)
        }
    }

    override fun trackNotificationOpen(inboxMessage: InboxMessage, completionListener: CompletionListener) {
        runnerProxy.logException {
            inboxInternal.trackNotificationOpen(inboxMessage, completionListener)
        }
    }

    override fun trackNotificationOpen(inboxMessage: InboxMessage, completionListener: (Throwable?) -> Unit) {
        val javaCompletionListener = CompletionListener { it -> completionListener.invoke(it) }
        runnerProxy.logException {
            inboxInternal.trackNotificationOpen(inboxMessage, javaCompletionListener)
        }
    }
}