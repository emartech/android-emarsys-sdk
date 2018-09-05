package com.emarsys.mobileengage.inbox;

import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;

public interface InboxInternal {
    void fetchNotifications(InboxResultListener<NotificationInboxStatus> resultListener);

    void resetBadgeCount(ResetBadgeCountResultListener listener);

    String trackMessageOpen(Notification message);

    void purgeNotificationCache();
}
