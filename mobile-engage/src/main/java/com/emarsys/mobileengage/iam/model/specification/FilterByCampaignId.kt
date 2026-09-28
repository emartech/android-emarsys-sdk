package com.emarsys.mobileengage.iam.model.specification

import com.emarsys.core.database.repository.AbstractSqlSpecification

class FilterByCampaignId(private vararg val campaignIds: String) : AbstractSqlSpecification() {

    private val sql: String = buildString {
        append("campaign_id IN (?")
        repeat(campaignIds.size - 1) { append(", ?") }
        append(")")
    }

    override val selection: String get() = sql
    override val selectionArgs: Array<String> get() = campaignIds as Array<String>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        return campaignIds.contentEquals((other as FilterByCampaignId).campaignIds)
    }

    override fun hashCode(): Int = campaignIds.contentHashCode()
}
