package com.emarsys.mobileengage.iam.model.specification;

import com.emarsys.core.database.repository.AbstractSqlSpecification;

import java.util.Arrays;

public class FilterByCampaignId extends AbstractSqlSpecification {

    private final String[] campaignIds;
    private final String sql;

    public FilterByCampaignId(String... campaignIds) {
        this.campaignIds = campaignIds;
        this.sql = createSql(campaignIds);
    }

    @Override
    public String getSelection() {
        return sql;
    }

    @Override
    public String[] getSelectionArgs() {
        return campaignIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterByCampaignId that = (FilterByCampaignId) o;

        return Arrays.equals(campaignIds, that.campaignIds);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(campaignIds);
    }

    private String createSql(String[] args) {
        StringBuilder sb = new StringBuilder("campaign_id IN (?");
        for (int i = 1; i < args.length; i++) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }
}
