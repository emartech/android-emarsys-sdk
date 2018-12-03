package com.emarsys.mobileengage.notification.command;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DismissNotificationCommandTest {

    @Test(expected = IllegalArgumentException.class)
    public void testDismissNotification_context_mustNotBeNull() {
        new DismissNotificationCommand(null, new Intent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDismissNotification_intent_mustNotBeNull() {
        new DismissNotificationCommand(mock(Context.class), null);
    }

    @Test
    public void testDismissNotification_callsNotificationManager() {
        int notificationId = 987;

        NotificationManager notificationManagerMock = mock(NotificationManager.class);

        Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManagerMock);

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("notification_id", notificationId);
        intent.putExtra("payload", bundle);

        new DismissNotificationCommand(mockContext, intent).run();

        verify(notificationManagerMock).cancel(notificationId);
    }

    @Test
    public void testDismissNotification_doesNotCallNotificationManager_ifBundleIsMissing() {
        NotificationManager notificationManagerMock = mock(NotificationManager.class);

        Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManagerMock);

        Intent intent = new Intent();

        new DismissNotificationCommand(mockContext, intent).run();

        verifyZeroInteractions(notificationManagerMock);
    }

    @Test
    public void testDismissNotification_doesNotCallNotificationManager_ifNotificationIdIsMissing() {
        NotificationManager notificationManagerMock = mock(NotificationManager.class);

        Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManagerMock);

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        intent.putExtra("payload", bundle);

        new DismissNotificationCommand(mockContext, intent).run();

        verifyZeroInteractions(notificationManagerMock);
    }

}