package com.emarsys.mobileengage.api.inbox

data class InboxMessage(
        val id: String,
        val multichannelId: Int?,
        val campaignId: String?,
        val title: String,
        val body: String,
        val imageUrl: String?,
        val action: String?,
        val receivedAt: Long,
        val updatedAt: Long?,
        val ttl: Int?,
        val tags: List<String>?,
        val sourceId: Int,
        val sourceRunId: String?,
        val sourceType: String
)