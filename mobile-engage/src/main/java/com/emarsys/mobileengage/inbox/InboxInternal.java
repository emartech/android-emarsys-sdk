package com.emarsys.mobileengage.inbox;

import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;

public interface InboxInternal {
    void fetchNotifications(InboxResultListener<NotificationInboxStatus> resultListener);

    void resetBadgeCount(ResetBadgeCountResultListener listener);

    String trackMessageOpen(Notification message);

    void purgeNotificationCache();
}
