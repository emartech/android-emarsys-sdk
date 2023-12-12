package com.emarsys.mobileengage.service.mapper

import com.emarsys.mobileengage.R
import com.emarsys.mobileengage.service.NotificationData

interface RemoteMessageMapper {

    companion object {
        const val METADATA_SMALL_NOTIFICATION_ICON_KEY =
            "com.emarsys.mobileengage.small_notification_icon"
        const val METADATA_NOTIFICATION_COLOR = "com.emarsys.mobileengage.notification_color"
        val DEFAULT_SMALL_NOTIFICATION_ICON = R.drawable.default_small_notification_icon
    }

    fun map(remoteMessageData: Map<String, String?>): NotificationData
}