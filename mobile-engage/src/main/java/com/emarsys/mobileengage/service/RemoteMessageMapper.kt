package com.emarsys.mobileengage.service

import android.content.Context
import com.emarsys.core.Mockable
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.ImageUtils
import com.emarsys.mobileengage.R
import org.json.JSONObject

@Mockable
class RemoteMessageMapper(
    private val metaDataReader: MetaDataReader,
    private val context: Context,
    private val fileDownloader: FileDownloader,
    private val deviceInfo: DeviceInfo
) {

    private companion object {
        const val METADATA_SMALL_NOTIFICATION_ICON_KEY =
            "com.emarsys.mobileengage.small_notification_icon"
        const val METADATA_NOTIFICATION_COLOR = "com.emarsys.mobileengage.notification_color"
        val DEFAULT_SMALL_NOTIFICATION_ICON = R.drawable.default_small_notification_icon
    }

    fun map(remoteMessageData: Map<String, String?>): NotificationData {
        val smallIconResourceId = metaDataReader.getInt(
            context,
            METADATA_SMALL_NOTIFICATION_ICON_KEY,
            DEFAULT_SMALL_NOTIFICATION_ICON
        )
        val colorResourceId = metaDataReader.getInt(context, METADATA_NOTIFICATION_COLOR)
        val image = ImageUtils.loadOptimizedBitmap(
            fileDownloader,
            remoteMessageData["image_url"],
            deviceInfo
        )
        val iconImage = ImageUtils.loadOptimizedBitmap(
            fileDownloader,
            remoteMessageData["icon_url"],
            deviceInfo
        )
        val title = remoteMessageData["title"]
        val ems = extractEms(remoteMessageData)
        val style = ems.optString("style")
        val body = remoteMessageData["body"]
        val channelId = remoteMessageData["channel_id"]
        val notificationMethod: NotificationMethod = if (ems.has("notificationMethod")) {
            parseNotificationMethod(ems.optJSONObject("notificationMethod"))
        } else {
            createNotificationMethod()
        }

        return NotificationData(
            image,
            iconImage,
            style,
            title,
            body,
            channelId,
            smallIconResourceId,
            colorResourceId,
            notificationMethod
        )
    }

    private fun parseNotificationMethod(notificationMethod: JSONObject): NotificationMethod {
        return if (notificationMethod.has("collapseId")) {
            val collapseId = notificationMethod.getInt("collapseId")
            val operation: NotificationOperation = parseOperation(notificationMethod)
            NotificationMethod(collapseId, operation)
        } else {
            createNotificationMethod()
        }
    }

    private fun createNotificationMethod(): NotificationMethod {
        val collapseId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        return NotificationMethod(collapseId, NotificationOperation.INIT)
    }

    private fun parseOperation(notificationMethod: JSONObject): NotificationOperation {
        return if (notificationMethod.has("operation")) {
            NotificationOperation.valueOf(notificationMethod.getString("operation").uppercase())
        } else NotificationOperation.INIT
    }

    private fun extractEms(remoteMessageData: Map<String, String?>) =
        JSONObject(remoteMessageData["ems"] ?: "{}")
}