package com.emarsys.mobileengage.service.mapper

import android.content.Context
import com.emarsys.core.Mockable
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.resource.MetaDataReader
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
    private val uuidProvider: UUIDProvider
) : RemoteMessageMapper {
    companion object {
        const val MISSING_SID = "Missing sid"
        const val MISSING_MESSAGE_ID = "Missing messageId"
        const val EMPTY_U = "{}"
    }

    override fun map(remoteMessageData: Map<String, String?>): NotificationData {
        val resourceIds = getNotificationResourceIds()
        val messageDataCopy = remoteMessageData.toMutableMap()

        val messageId = messageDataCopy.remove("message_id") ?: MISSING_MESSAGE_ID
        val image = messageDataCopy.remove("image_url")
        val iconImage = messageDataCopy.remove("icon_url")
        val title = messageDataCopy.remove("title")
        val ems = JSONObject(messageDataCopy.remove("ems") ?: "{}")
        val style = ems.optString("style")
        val campaignId = ems.optString("multichannelId")
        val body = messageDataCopy.remove("body")
        val channelId = messageDataCopy.remove("channel_id")
        val u = messageDataCopy.remove("u") ?: EMPTY_U
        val sid = JSONObject(u).getNullableString("sid") ?: MISSING_SID
        val notificationMethod: NotificationMethod = if (ems.has("notificationMethod")) {
            parseNotificationMethod(ems.optJSONObject("notificationMethod"))
        } else {
            createNotificationMethod()
        }
        val actions = ems.getNullableString("actions")
        val defaultAction = ems.getNullableString("default_action")
        val inapp = ems.getNullableString("inapp")

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
            rootParams = messageDataCopy,
            u = u,
            message_id = messageId
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
}