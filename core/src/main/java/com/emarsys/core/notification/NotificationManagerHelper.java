package com.emarsys.core.notification;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.emarsys.core.api.notification.ChannelSettings;
import com.emarsys.core.api.notification.NotificationSettings;
import com.emarsys.core.util.Assert;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationManagerHelper that = (NotificationManagerHelper) o;
        return Objects.equals(notificationManagerProxy, that.notificationManagerProxy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationManagerProxy);
    }
}
