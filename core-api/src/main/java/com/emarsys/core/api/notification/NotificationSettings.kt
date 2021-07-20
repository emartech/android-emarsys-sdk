package com.emarsys.core.api.notification;

import java.util.List;

public interface NotificationSettings {

    boolean areNotificationsEnabled();

    int getImportance();

    List<ChannelSettings> getChannelSettings();

}
