package com.emarsys.mobileengage.inbox

import com.emarsys.core.Mapper
import com.emarsys.core.Mockable
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.JsonUtils
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.api.inbox.Message
import org.json.JSONException
import org.json.JSONObject

@Mockable
class MessageInboxResponseMapper : Mapper<ResponseModel, InboxResult> {

    override fun map(responseModel: ResponseModel?): InboxResult {
        val inboxMessages = mutableListOf<Message>()

        val messageInboxResult = responseModel?.parsedBody
        val notificationsResponse = messageInboxResult?.optJSONArray("messages")
        if (notificationsResponse != null) {
            for (i in 0 until notificationsResponse.length()) {
                try {
                    val inboxMessageResponse = notificationsResponse.getJSONObject(i)
                    val inboxMessage = inboxMessage(inboxMessageResponse)
                    inboxMessages.add(inboxMessage)
                } catch (ignored: JSONException) {
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
                if (inboxMessageResponse.isNull("collapseId")) null else inboxMessageResponse.getString("collapseId"),
                inboxMessageResponse.getString("title"),
                inboxMessageResponse.getString("body"),
                if (inboxMessageResponse.isNull("imageUrl")) null else inboxMessageResponse.getString("imageUrl"),
                inboxMessageResponse.getLong("receivedAt"),
                inboxMessageResponse.getLong("updatedAt"),
                if (inboxMessageResponse.isNull("expiresAt")) null else inboxMessageResponse.getLong("expiresAt"),
                if (inboxMessageResponse.isNull("tags")) null else tags,
                if (inboxMessageResponse.isNull("properties")) null else JsonUtils.toFlatMap(inboxMessageResponse.getJSONObject("properties"))
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

}