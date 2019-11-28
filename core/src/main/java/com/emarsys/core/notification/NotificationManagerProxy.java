package com.emarsys.core.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.emarsys.core.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationManagerProxy {
    private NotificationManager notificationManager;
    private NotificationManagerCompat notificationManagerCompat;

    public NotificationManagerProxy(NotificationManager notificationManager, NotificationManagerCompat notificationManagerCompat) {
        Assert.notNull(notificationManager, "NotificationManager must not be null!");
        Assert.notNull(notificationManagerCompat, "NotificationManagerCompat must not be null!");

        this.notificationManager = notificationManager;
        this.notificationManagerCompat = notificationManagerCompat;
    }

    public boolean areNotificationsEnabled() {
        return notificationManagerCompat.areNotificationsEnabled();
    }

    public int getImportance() {
        return notificationManagerCompat.getImportance();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<ChannelSettings> getNotificationChannels() {
        List<ChannelSettings> channelSettings = new ArrayList<>();

        for (NotificationChannel notificationChannel : notificationManager.getNotificationChannels()) {
            channelSettings.add(new ChannelSettings(notificationChannel.getId(),
                    notificationChannel.getImportance(),
                    notificationChannel.canBypassDnd(),
                    notificationChannel.canShowBadge(),
                    notificationChannel.shouldVibrate(),
                    notificationChannel.shouldShowLights()));
        }
        return channelSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationManagerProxy that = (NotificationManagerProxy) o;
        return Objects.equals(notificationManager, that.notificationManager) &&
                Objects.equals(notificationManagerCompat, that.notificationManagerCompat) &&
                Objects.equals(areNotificationsEnabled(), that.areNotificationsEnabled());
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationManager, notificationManagerCompat, areNotificationsEnabled());
    }
}
