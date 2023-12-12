package com.emarsys.mobileengage.service

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationData(
    val image: Bitmap? = null,
    val iconImage: Bitmap? = null,
    val style: String? = null,
    val title: String? = null,
    val body: String? = null,
    val channelId: String? = null,
    val campaignId: String? = null,
    val sid: String,
    val smallIconResourceId: Int,
    val colorResourceId: Int,
    val notificationMethod: NotificationMethod,
    val actions: String? = null,
    val defaultAction: String? = null,
    val inapp: String? = null
) : Parcelable

enum class NotificationOperation {
    INIT, UPDATE, DELETE
}

@Parcelize
data class NotificationMethod(
    val collapseId: String,
    val operation: NotificationOperation
) : Parcelable

data class NotificationResourceIds(
    val smallIconResourceId: Int,
    val colorResourceId: Int
)