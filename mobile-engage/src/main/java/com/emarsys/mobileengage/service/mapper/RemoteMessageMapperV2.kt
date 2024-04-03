package com.emarsys.mobileengage.service.mapper

import android.content.Context
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.resource.MetaDataReader
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
    private val uuidProvider: UUIDProvider
) : RemoteMessageMapper {
    override fun map(remoteMessageData: Map<String, String?>): NotificationData {
        val resourceIds = getNotificationResourceIds()

        val image = remoteMessageData["notification.image"]
        val iconImage = remoteMessageData["notification.icon"]
        val title = remoteMessageData["notification.title"]
        val style = remoteMessageData["ems.style"]
        val campaignId = remoteMessageData["ems.multichannel_id"]
        val body = remoteMessageData["notification.body"]
        val channelId = remoteMessageData["notification.channel_id"]
        val notificationMethod: NotificationMethod = parseNotificationMethod(remoteMessageData)
        val sid = remoteMessageData["ems.sid"] ?: "Missing sid"
        val actions = remoteMessageData["ems.actions"]
        val defaultAction = extractDefaultAction(remoteMessageData)
        val inapp = remoteMessageData["ems.inapp"]
        val rootParams = extractRootParams(remoteMessageData["ems.root_params"])

        return NotificationData(
            imageUrl = image,
            iconImageUrl = iconImage,
            style = style,
            title = title,
            body = body,
            channelId = channelId,
            campaignId = campaignId,
            sid = sid,
            smallIconResourceId = resourceIds.smallIconResourceId,
            colorResourceId = resourceIds.colorResourceId,
            collapseId = notificationMethod.collapseId,
            operation = notificationMethod.operation.name,
            actions = actions,
            defaultAction = defaultAction,
            inapp = inapp,
            rootParams
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

    private fun extractDefaultAction(remoteMessageData: Map<String, String?>): String? {
        val name = remoteMessageData.getOrDefault("ems.tap_actions.default_action.name", null)
        val type = remoteMessageData.getOrDefault("ems.tap_actions.default_action.type", null)
        val url = remoteMessageData.getOrDefault("ems.tap_actions.default_action.url", null)
        val payload =
            remoteMessageData.getOrDefault("ems.tap_actions.default_action.payload", null)
        val payloadObject = payload?.let { JSONObject(payload) }

        val defaultAction = if (type != null) {
            JSONObject()
                .put("name", name)
                .put("type", type)
                .put("url", url)
                .put("payload", payloadObject)
        } else null

        return defaultAction?.toString()
    }

    private fun extractRootParams(rootParams: String?): Map<String, String?> {
        val mappedRootParams = mutableMapOf<String, String?>()
        val json = rootParams?.let {
            try {
                JSONObject(it)
            } catch (ignored: Exception) {
                JSONObject("{}")
            }
        }

        json?.let {
            it.keys().forEach { key ->
                mappedRootParams[key] = it[key].toString()
            }
        }

        return mappedRootParams
    }
}