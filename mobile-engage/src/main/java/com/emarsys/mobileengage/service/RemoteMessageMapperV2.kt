package com.emarsys.mobileengage.service

import android.content.Context
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.ImageUtils
import org.json.JSONObject

class RemoteMessageMapperV2(
    private val metaDataReader: MetaDataReader,
    private val context: Context,
    private val fileDownloader: FileDownloader,
    private val deviceInfo: DeviceInfo,
    private val uuidProvider: UUIDProvider
) : RemoteMessageMapper {
    override fun map(remoteMessageData: Map<String, String?>): NotificationData {
        val resourceIds = getNotificationResourceIds()

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
            resourceIds.smallIconResourceId,
            resourceIds.colorResourceId,
            notificationMethod
        )
    }

    private fun getNotificationResourceIds(): NotificationResourceIds {
        val smallIconResourceId = metaDataReader.getInt(
            context,
            RemoteMessageMapperV1.METADATA_SMALL_NOTIFICATION_ICON_KEY,
            RemoteMessageMapperV1.DEFAULT_SMALL_NOTIFICATION_ICON
        )
        val colorResourceId = metaDataReader.getInt(context,
            RemoteMessageMapperV1.METADATA_NOTIFICATION_COLOR
        )

        return NotificationResourceIds(smallIconResourceId, colorResourceId)
    }

    private fun parseNotificationMethod(notificationMethod: JSONObject): NotificationMethod {
        return if (notificationMethod.has("collapseId")) {
            val collapseId = notificationMethod.getString("collapseId")
            val operation: NotificationOperation = parseOperation(notificationMethod)
            NotificationMethod(collapseId, operation)
        } else {
            createNotificationMethod()
        }
    }

    private fun createNotificationMethod(): NotificationMethod {
        return NotificationMethod(uuidProvider.provideId(), NotificationOperation.INIT)
    }

    private fun parseOperation(notificationMethod: JSONObject): NotificationOperation {
        return if (notificationMethod.has("operation")) {
            NotificationOperation.valueOf(notificationMethod.getString("operation").uppercase())
        } else NotificationOperation.INIT
    }

    private fun extractEms(remoteMessageData: Map<String, String?>) =
        JSONObject(remoteMessageData["ems"] ?: "{}")
}