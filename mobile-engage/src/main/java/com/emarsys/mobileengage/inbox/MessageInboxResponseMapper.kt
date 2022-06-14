package com.emarsys.mobileengage.inbox

import com.emarsys.core.Mapper
import com.emarsys.core.Mockable
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.JsonUtils
import com.emarsys.core.util.JsonUtils.toMutableList
import com.emarsys.core.util.getNullableLong
import com.emarsys.core.util.getNullableString
import com.emarsys.mobileengage.api.action.*
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.api.inbox.Message
import org.json.JSONException
import org.json.JSONObject
import java.net.URL

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
@Mockable
class MessageInboxResponseMapper : Mapper<ResponseModel, InboxResult> {

    override fun map(responseModel: ResponseModel): InboxResult {
        val inboxMessages = mutableListOf<Message>()

        val messageInboxResult = responseModel.parsedBody
        val notificationsResponse = messageInboxResult?.optJSONArray("messages")
        if (notificationsResponse != null) {
            for (i in 0 until notificationsResponse.length()) {
                try {
                    val inboxMessageResponse = notificationsResponse.getJSONObject(i)
                    val inboxMessage = inboxMessage(inboxMessageResponse)
                    inboxMessages.add(inboxMessage)
                } catch (ignored: JSONException) {
                    ignored.printStackTrace()
                }
            }
        }
        return InboxResult(inboxMessages)
    }

    private fun inboxMessage(inboxMessageResponse: JSONObject): Message {
        val tags = parseTags(inboxMessageResponse)

        return Message(
            inboxMessageResponse.getString("id"),
            inboxMessageResponse.getString("campaignId"),
            if (inboxMessageResponse.isNull("collapseId")) null else inboxMessageResponse.getString(
                "collapseId"
            ),
            inboxMessageResponse.getString("title"),
            inboxMessageResponse.getString("body"),
            inboxMessageResponse.getNullableString("imageUrl"),
            inboxMessageResponse.getLong("receivedAt"),
            inboxMessageResponse.getLong("updatedAt"),
            inboxMessageResponse.getNullableLong("expiresAt"),
            if (inboxMessageResponse.isNull("tags")) null else tags,
            if (inboxMessageResponse.isNull("properties")) null else JsonUtils.toFlatMap(
                inboxMessageResponse.getJSONObject("properties")
            ),
            inboxMessageResponse.optJSONObject("ems")
                ?.optJSONArray("actions")
                ?.let {
                    it.toMutableList().mapNotNull { jsonObject -> createActionModel(jsonObject) }
                }
        )
    }

    private fun parseTags(inboxMessageResponse: JSONObject): List<String> {
        val tags = inboxMessageResponse.optJSONArray("tags")

        val tagsList = mutableListOf<String>()
        if (tags != null) {
            for (i in 0 until tags.length()) {
                tagsList.add(tags.getString(i))
            }
        }
        return tagsList
    }

    private fun createActionModel(actionJson: JSONObject): ActionModel? {
        return when (actionJson.optString("type")) {
            "MEAppEvent" -> AppEventActionModel(
                actionJson.getString("id"),
                actionJson.getString("title"),
                actionJson.getString("type"),
                actionJson.getString("name"),
                JsonUtils.toMap(
                    if (actionJson.has("payload") && !actionJson.isNull("payload")) actionJson.getJSONObject(
                        "payload"
                    ) else JSONObject()
                )
            )
            "OpenExternalUrl" -> OpenExternalUrlActionModel(
                actionJson.getString("id"),
                actionJson.getString("title"),
                actionJson.getString("type"),
                URL(actionJson.getString("url"))
            )
            "MECustomEvent" -> CustomEventActionModel(
                actionJson.getString("id"),
                actionJson.getString("title"),
                actionJson.getString("type"),
                actionJson.getString("name"),
                JsonUtils.toMap(actionJson.getJSONObject("payload"))
            )
            "Dismiss" -> DismissActionModel(
                actionJson.getString("id"),
                actionJson.getString("title"),
                actionJson.getString("type")
            )
            else -> null
        }
    }

}