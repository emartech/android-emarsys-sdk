package com.emarsys.core.notification;

import java.util.List;

public interface NotificationSettings {

    boolean areNotificationsEnabled();

    int getImportance();

    List<ChannelSettings> getChannelSettings();

}
