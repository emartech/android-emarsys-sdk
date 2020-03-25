package com.emarsys.inbox

import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.MessageInboxResult

interface MessageInboxApi {
    fun fetchNotifications(resultListener: ResultListener<Try<MessageInboxResult>>)

    fun fetchNotifications(resultListener: (Try<MessageInboxResult>) -> Unit)
}