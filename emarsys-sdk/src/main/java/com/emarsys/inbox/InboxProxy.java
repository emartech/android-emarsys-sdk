package com.emarsys.inbox;


import androidx.annotation.NonNull;

import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.inbox.InboxInternal;

public class InboxProxy implements InboxApi {

    private final RunnerProxy runnerProxy;
    private final InboxInternal inboxInternal;

    public InboxProxy(RunnerProxy runnerProxy, InboxInternal inboxInternal) {
        Assert.notNull(runnerProxy, "RunnerProxy must not be null!");
        Assert.notNull(inboxInternal, "InboxInternal must not be null!");

        this.runnerProxy = runnerProxy;
        this.inboxInternal = inboxInternal;
    }

    public void fetchNotifications(
            @NonNull final ResultListener<Try<NotificationInboxStatus>> resultListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(resultListener, "ResultListener must not be null!");

                inboxInternal.fetchNotifications(resultListener);
            }
        });
    }

    public void trackNotificationOpen(@NonNull final Notification notification) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(notification, "Notification must not be null!");

                inboxInternal.trackNotificationOpen(notification, null);
            }
        });
    }

    public void trackNotificationOpen(
            @NonNull final Notification notification,
            @NonNull final CompletionListener completionListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(notification, "Notification must not be null!");
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                inboxInternal.trackNotificationOpen(notification, completionListener);
            }
        });
    }

    public void resetBadgeCount() {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                inboxInternal.resetBadgeCount(null);
            }
        });
    }

    public void resetBadgeCount(@NonNull final CompletionListener completionListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                inboxInternal.resetBadgeCount(completionListener);
            }
        });
    }
}