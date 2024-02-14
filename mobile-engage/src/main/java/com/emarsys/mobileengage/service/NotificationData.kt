package com.emarsys.mobileengage.service

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationData(
    val imageUrl: String? = null,
    val iconImageUrl: String? = null,
    val style: String? = null,
    val title: String? = null,
    val body: String? = null,
    val channelId: String? = null,
    val campaignId: String? = null,
    val sid: String,
    val smallIconResourceId: Int,
    val colorResourceId: Int,
    val collapseId: String,
    val operation: String,
    val actions: String? = null,
    val defaultAction: String? = null,
    val inapp: String? = null,
    val rootParams: Map<String, String?> = mapOf()
) : Parcelable

enum class NotificationOperation {
    INIT, UPDATE, DELETE
}

data class NotificationMethod(
    val collapseId: String,
    val operation: NotificationOperation
)

data class NotificationResourceIds(
    val smallIconResourceId: Int,
    val colorResourceId: Int
)