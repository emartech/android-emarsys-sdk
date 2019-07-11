package com.emarsys;

import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.inbox.InboxProxy;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InboxProxyTest {
    private InboxProxy inboxProxy;

    private InboxInternal mockInboxInternal;

    private RunnerProxy runnerProxy;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        runnerProxy = new RunnerProxy();
        mockInboxInternal = mock(InboxInternal.class);

        inboxProxy = new InboxProxy(runnerProxy, mockInboxInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_runnerProxy_mustNotBeNull() {
        new InboxProxy(null, mockInboxInternal);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_inboxInternal_mustNotBeNull() {
        new InboxProxy(runnerProxy, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_fetchNotifications_resultListener_mustNotBeNull() {
        inboxProxy.fetchNotifications(null);
    }

    @Test
    public void testInbox_fetchNotifications_delegatesTo_inboxInternal() {
        ResultListener<Try<NotificationInboxStatus>> resultListener = new ResultListener<Try<NotificationInboxStatus>>() {
            @Override
            public void onResult(Try<NotificationInboxStatus> result) {
            }
        };

        inboxProxy.fetchNotifications(resultListener);
        verify(mockInboxInternal).fetchNotifications(resultListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_trackNotificationOpen_notification_mustNotBeNull() {
        inboxProxy.trackNotificationOpen(null);
    }

    @Test
    public void testInbox_trackNotificationOpen_delegatesTo_inboxInternal() {
        Notification notification = mock(Notification.class);

        inboxProxy.trackNotificationOpen(notification);
        verify(mockInboxInternal).trackNotificationOpen(notification, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_trackNotificationOpen_notification_resultListener_notification_mustNotBeNull() {
        inboxProxy.trackNotificationOpen(null, mock(CompletionListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_trackNotificationOpen_notification_resultListener_resultListener_mustNotBeNull() {
        inboxProxy.trackNotificationOpen(mock(Notification.class), null);
    }

    @Test
    public void testInbox_trackNotificationOpen_notification_resultListener_delegatesTo_inboxInternal() {
        Notification notification = mock(Notification.class);
        CompletionListener resultListener = mock(CompletionListener.class);

        inboxProxy.trackNotificationOpen(notification, resultListener);

        verify(mockInboxInternal).trackNotificationOpen(notification, resultListener);
    }

    @Test
    public void testInbox_resetBadgeCount_delegatesTo_inboxInternal() {
        inboxProxy.resetBadgeCount();

        verify(mockInboxInternal).resetBadgeCount(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInbox_resetBadgeCount_withCompletionListener_resultListener_mustNotBeNull() {
        inboxProxy.resetBadgeCount(null);
    }

    @Test
    public void testInbox_resetBadgeCount_withCompletionListener_delegatesTo_inboxInternal() {
        CompletionListener mockResultListener = mock(CompletionListener.class);

        inboxProxy.resetBadgeCount(mockResultListener);

        verify(mockInboxInternal).resetBadgeCount(mockResultListener);
    }
}