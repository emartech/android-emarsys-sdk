package com.emarsys.mobileengage.iam.model.specification;

import com.emarsys.core.database.repository.SqlSpecification;

import java.util.Arrays;

public class FilterByCampaignId implements SqlSpecification {

    private final String[] campaignIds;
    private final String sql;

    public FilterByCampaignId(String... campaignIds) {
        this.campaignIds = campaignIds;
        this.sql = createSql(campaignIds);
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public String[] getArgs() {
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
