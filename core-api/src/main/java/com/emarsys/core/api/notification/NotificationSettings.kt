package com.emarsys.core.api.notification

interface NotificationSettings {
    val areNotificationsEnabled: Boolean
    val importance: Int
    val channelSettings: List<ChannelSettings>
}