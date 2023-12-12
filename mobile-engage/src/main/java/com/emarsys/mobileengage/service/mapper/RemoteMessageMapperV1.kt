package com.emarsys.mobileengage.service.mapper

import android.content.Context
import com.emarsys.core.Mockable
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.resource.MetaDataReader
import com.emarsys.core.util.FileDownloader
import com.emarsys.core.util.ImageUtils
import com.emarsys.core.util.getNullableString
import com.emarsys.mobileengage.service.NotificationData
import com.emarsys.mobileengage.service.NotificationMethod
import com.emarsys.mobileengage.service.NotificationOperation
import com.emarsys.mobileengage.service.NotificationResourceIds
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapper.Companion.DEFAULT_SMALL_NOTIFICATION_ICON
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapper.Companion.METADATA_NOTIFICATION_COLOR
import com.emarsys.mobileengage.service.mapper.RemoteMessageMapper.Companion.METADATA_SMALL_NOTIFICATION_ICON_KEY
import org.json.JSONObject

@Mockable
class RemoteMessageMapperV1(
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
        val campaignId = ems.optString("multichannelId")
        val body = remoteMessageData["body"]
        val channelId = remoteMessageData["channel_id"]
        val sid = remoteMessageData["u"]?.let { JSONObject(it).getNullableString("sid") } ?: "Missing sid"
        val notificationMethod: NotificationMethod = if (ems.has("notificationMethod")) {
            parseNotificationMethod(ems.optJSONObject("notificationMethod"))
        } else {
            createNotificationMethod()
        }
        val actions = ems.getNullableString("actions")
        val defaultAction = ems.getNullableString("default_action")
        val inapp = ems.getNullableString("inapp")

        return NotificationData(
            image,
            iconImage,
            style,
            title,
            body,
            channelId,
            campaignId,
            sid,
            resourceIds.smallIconResourceId,
            resourceIds.colorResourceId,
            notificationMethod,
            actions,
            defaultAction,
            inapp
        )
    }

    private fun getNotificationResourceIds(): NotificationResourceIds {
        val smallIconResourceId = metaDataReader.getInt(
            context,
            METADATA_SMALL_NOTIFICATION_ICON_KEY,
            DEFAULT_SMALL_NOTIFICATION_ICON
        )
        val colorResourceId = metaDataReader.getInt(context, METADATA_NOTIFICATION_COLOR)

        return NotificationResourceIds(smallIconResourceId, colorResourceId)
    }

    private fun parseNotificationMethod(notificationMethod: JSONObject?): NotificationMethod {
        return if (notificationMethod != null && notificationMethod.has("collapseId")) {
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