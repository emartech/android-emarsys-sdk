package com.emarsys.core.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.emarsys.core.util.Assert;

import java.util.List;

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
    public List<NotificationChannel> getNotificationChannels() {
        return notificationManager.getNotificationChannels();
    }
}
