package com.emarsys.mobileengage.inbox;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;

public interface InboxInternal {
    void fetchNotifications(ResultListener<Try<NotificationInboxStatus>> resultListener);

    void resetBadgeCount(CompletionListener completionListener);

    void trackNotificationOpen(Notification notification, CompletionListener completionListener);

    void purgeNotificationCache();
}
