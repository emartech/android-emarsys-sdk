package com.emarsys.inbox

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.mobileengage.di.mobileEngage

@Mockable
class Inbox(private val loggingInstance: Boolean = false) : InboxApi {
    override fun fetchNotifications(
            resultListener: ResultListener<Try<NotificationInboxStatus>>) {
        (if (loggingInstance) mobileEngage().loggingInboxInternal else mobileEngage().inboxInternal)
                .fetchNotifications(resultListener)
    }

    override fun trackNotificationOpen(notification: Notification) {
        (if (loggingInstance) mobileEngage().loggingInboxInternal else mobileEngage().inboxInternal)
                .trackNotificationOpen(notification, null)
    }

    override fun trackNotificationOpen(
            notification: Notification,
            completionListener: CompletionListener) {
        (if (loggingInstance) mobileEngage().loggingInboxInternal else mobileEngage().inboxInternal)
                .trackNotificationOpen(notification, completionListener)
    }

    override fun resetBadgeCount() {
        (if (loggingInstance) mobileEngage().loggingInboxInternal else mobileEngage().inboxInternal)
                .resetBadgeCount(null)
    }

    override fun resetBadgeCount(completionListener: CompletionListener) {
        (if (loggingInstance) mobileEngage().loggingInboxInternal else mobileEngage().inboxInternal)
                .resetBadgeCount(completionListener)
    }

}