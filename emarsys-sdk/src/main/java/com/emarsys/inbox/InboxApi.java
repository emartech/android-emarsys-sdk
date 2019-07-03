package com.emarsys.inbox;

import androidx.annotation.NonNull;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;

public interface InboxApi {

    void fetchNotifications(@NonNull final ResultListener<Try<NotificationInboxStatus>> resultListener);

    void trackNotificationOpen(@NonNull final Notification notification);

    void trackNotificationOpen(@NonNull final Notification notification,
                               @NonNull final CompletionListener completionListener);

    void resetBadgeCount();

    void resetBadgeCount(@NonNull final CompletionListener completionListener);

}
