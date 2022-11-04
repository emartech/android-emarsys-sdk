package com.emarsys.mobileengage.service

import android.graphics.Bitmap

data class NotificationData(
    val image: Bitmap? = null,
    val iconImage: Bitmap? = null,
    val style: String? = null,
    val title: String? = null,
    val body: String? = null,
    val channelId: String? = null,
    val smallIconResourceId: Int,
    val colorResourceId: Int,
    val notificationMethod: NotificationMethod? = null
)

enum class NotificationOperation {
    UPDATE, DELETE
}

data class NotificationMethod(
    val notificationId: Int,
    val operation: NotificationOperation
)