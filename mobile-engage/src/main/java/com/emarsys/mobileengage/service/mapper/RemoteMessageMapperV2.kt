package com.emarsys.mobileengage.service.mapper

import android.content.Context
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
            remoteMessageData["notification.image"],
            deviceInfo
        )
        val iconImage = ImageUtils.loadOptimizedBitmap(
            fileDownloader,
            remoteMessageData["notification.icon"],
            deviceInfo
        )
        val title = remoteMessageData["notification.title"]
        val style = remoteMessageData["ems.style"]
        val campaignId = remoteMessageData["ems.multichannel_id"]
        val body = remoteMessageData["notification.body"]
        val channelId = remoteMessageData["notification.channel_id"]
        val notificationMethod: NotificationMethod = parseNotificationMethod(remoteMessageData)
        val sid = remoteMessageData["ems.sid"] ?: "Missing sid"
        val emsRootParams = extractEmsRootParams(remoteMessageData)
        val actions = emsRootParams.getNullableString("actions")
        val defaultAction = extractDefaultAction(remoteMessageData)
        val inapp = emsRootParams.getNullableString("inapp")

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
        val colorResourceId = metaDataReader.getInt(
            context,
            METADATA_NOTIFICATION_COLOR
        )

        return NotificationResourceIds(smallIconResourceId, colorResourceId)
    }

    private fun parseNotificationMethod(
        remoteMessageData: Map<String, String?>
    ): NotificationMethod {
        val collapseId = remoteMessageData["ems.notification_method.collapse_key"]
        val parsedOperation = parseOperation(remoteMessageData["ems.notification_method.operation"])
        return if (collapseId != null) {
            NotificationMethod(collapseId, parsedOperation)
        } else {
            createNotificationMethod()
        }
    }

    private fun createNotificationMethod(): NotificationMethod {
        return NotificationMethod(uuidProvider.provideId(), NotificationOperation.INIT)
    }

    private fun parseOperation(notificationMethod: String?): NotificationOperation {
        return if (notificationMethod != null) {
            NotificationOperation.valueOf(notificationMethod.uppercase())
        } else NotificationOperation.INIT
    }

    private fun extractEmsRootParams(remoteMessageData: Map<String, String?>) =
        JSONObject(remoteMessageData["ems.root_params"] ?: "{}")

    private fun extractDefaultAction(remoteMessageData: Map<String, String?>): String? {
        val name = remoteMessageData.getOrDefault("ems.tap_actions.default_action.name", null)
        val type = remoteMessageData.getOrDefault("ems.tap_actions.default_action.type", null)
        val url = remoteMessageData.getOrDefault("ems.tap_actions.default_action.url", null)
        val payload =
            remoteMessageData.getOrDefault("ems.tap_actions.default_action.payload", null)

        val defaultAction = if (type != null) {
            JSONObject()
                .put("name", name)
                .put("type", type)
                .put("url", url)
                .put("payload", payload)
        } else null

        return defaultAction?.toString()
    }
}