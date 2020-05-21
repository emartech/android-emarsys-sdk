package com.emarsys.inbox

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.di.Container.getDependency
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.mobileengage.inbox.InboxInternal

@Mockable
class Inbox(private val loggingInstance: Boolean = false) : InboxApi {
    override fun fetchNotifications(
            resultListener: ResultListener<Try<NotificationInboxStatus>>) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<InboxInternal>("defaultInstance"))
                .fetchNotifications(resultListener)
    }

    override fun trackNotificationOpen(notification: Notification) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<InboxInternal>("defaultInstance"))
                .trackNotificationOpen(notification, null)
    }

    override fun trackNotificationOpen(
            notification: Notification,
            completionListener: CompletionListener) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<InboxInternal>("defaultInstance"))
                .trackNotificationOpen(notification, completionListener)
    }

    override fun resetBadgeCount() {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<InboxInternal>("defaultInstance"))
                .resetBadgeCount(null)
    }

    override fun resetBadgeCount(completionListener: CompletionListener) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<InboxInternal>("defaultInstance"))
                .resetBadgeCount(completionListener)
    }

}