package com.emarsys.mobileengage.inbox

import com.emarsys.core.Mapper
import com.emarsys.core.Mockable
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.api.inbox.InboxMessage
import com.emarsys.mobileengage.api.inbox.MessageInboxResult
import org.json.JSONObject

@Mockable
class MessageInboxResponseMapper : Mapper<ResponseModel, MessageInboxResult> {

    override fun map(responseModel: ResponseModel?): MessageInboxResult {
        val inboxMessages = mutableListOf<InboxMessage>()

        val messageInboxResult = responseModel?.parsedBody
        val notificationsResponse = messageInboxResult?.optJSONArray("notifications")
        if (notificationsResponse != null) {
            for (i in 0 until notificationsResponse.length()) {
                val inboxMessageResponse = notificationsResponse.getJSONObject(i)
                val inboxMessage = inboxMessage(inboxMessageResponse)
                inboxMessages.add(inboxMessage)
            }
        }
        return MessageInboxResult(inboxMessages)
    }

    private fun inboxMessage(inboxMessageResponse: JSONObject): InboxMessage {
        val tags = parseTags(inboxMessageResponse)

        return InboxMessage(
                inboxMessageResponse.getString("id"),
                 if (inboxMessageResponse.isNull("multichannelId")) null else inboxMessageResponse.getInt("multichannelId"),
                if(inboxMessageResponse.isNull("campaignId")) null else inboxMessageResponse.getString("campaignId"),
                inboxMessageResponse.getString("title"),
                inboxMessageResponse.getString("body"),
                if(inboxMessageResponse.isNull("imageUrl")) null else inboxMessageResponse.getString("imageUrl"),
                if(inboxMessageResponse.isNull("action")) null else inboxMessageResponse.getString("action"),
                inboxMessageResponse.getLong("receivedAt"),
                if(inboxMessageResponse.isNull("updatedAt")) null else inboxMessageResponse.getLong("updatedAt"),
                if (inboxMessageResponse.isNull("ttl")) null else inboxMessageResponse.getInt("ttl"),
                tags,
                inboxMessageResponse.getInt("sourceId"),
                if(inboxMessageResponse.isNull("sourceRunId")) null else inboxMessageResponse.getString("sourceRunId"),
                inboxMessageResponse.getString("sourceType")
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