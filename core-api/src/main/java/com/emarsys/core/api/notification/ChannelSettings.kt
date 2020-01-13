package com.emarsys.core.api.notification

data class ChannelSettings(val channelId: String,
                           val importance: Int = -1000,
                           val isCanBypassDnd: Boolean = false,
                           val isCanShowBadge: Boolean = false,
                           val isShouldVibrate: Boolean = false,
                           val isShouldShowLights: Boolean = false)