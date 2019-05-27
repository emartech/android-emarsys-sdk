package com.emarsys.core.notification;

import android.app.NotificationChannel;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.emarsys.core.util.Assert;

import java.util.ArrayList;
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
        List<NotificationChannel> notificationChannels = notificationManagerProxy.getNotificationChannels();
        List<ChannelSettings> channelSettings = new ArrayList<>();
        for (NotificationChannel notificationChannel : notificationChannels) {
            channelSettings.add(new ChannelSettings(notificationChannel.getId(),
                    notificationChannel.getImportance(),
                    notificationChannel.canBypassDnd(),
                    notificationChannel.canShowBadge(),
                    notificationChannel.shouldVibrate(),
                    notificationChannel.shouldShowLights()));
        }
        return channelSettings;
    }
}
