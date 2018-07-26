package com.emarsys.mobileengage.iam.model.displayediam;

public class DisplayedIam {
    private String campaignId;
    private long timestamp;

    public DisplayedIam(String campaignId, long timestamp) {
        this.campaignId = campaignId;
        this.timestamp = timestamp;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisplayedIam that = (DisplayedIam) o;

        if (timestamp != that.timestamp) return false;
        return campaignId != null ? campaignId.equals(that.campaignId) : that.campaignId == null;
    }

    @Override
    public int hashCode() {
        int result = campaignId != null ? campaignId.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DisplayedIam{" +
                "campaignId='" + campaignId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
