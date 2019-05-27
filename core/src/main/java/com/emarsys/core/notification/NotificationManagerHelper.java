package com.emarsys.core.notification;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.emarsys.core.util.Assert;

import java.util.List;

public class NotificationManagerHelper implements NotificationSettings {

    private NotificationManagerProxy notificationManagerProxy;

    public NotificationManagerHelper(NotificationManagerProxy notificationManagerProxy) {
        Assert.notNull(notificationManagerProxy, "NotificationManagerProxy must not be null!");

        this.notificationManagerProxy = notificationManagerProxy;
    }

    @Override
    public boolean areNotificationsEnabled() {
        return notificationManagerProxy.areNotificationsEnabled();
    }

    @Override
    public int getImportance() {
        return notificationManagerProxy.getImportance();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<ChannelSettings> getChannelSettings() {
        return notificationManagerProxy.getNotificationChannels();
    }
}
