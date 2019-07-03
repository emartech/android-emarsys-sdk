package com.emarsys.mobileengage.inbox;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;

public class LoggingInboxInternal implements InboxInternal {
    @Override
    public void fetchNotifications(ResultListener<Try<NotificationInboxStatus>> resultListener) {

    }

    @Override
    public void resetBadgeCount(CompletionListener completionListener) {

    }

    @Override
    public void trackNotificationOpen(Notification notification, CompletionListener completionListener) {

    }
}
