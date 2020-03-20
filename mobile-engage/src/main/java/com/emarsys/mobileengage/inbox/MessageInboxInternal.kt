package com.emarsys.mobileengage.inbox

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.InboxMessage
import com.emarsys.mobileengage.api.inbox.MessageInboxResult


interface MessageInboxInternal {
    fun fetchNotifications(resultListener: ResultListener<Try<MessageInboxResult>>)

    fun trackNotificationOpen(notification: InboxMessage, completionListener: CompletionListener?)
}