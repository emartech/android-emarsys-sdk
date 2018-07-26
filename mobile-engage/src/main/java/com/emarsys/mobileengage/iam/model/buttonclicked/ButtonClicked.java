package com.emarsys.mobileengage.iam.model.buttonclicked;

public class ButtonClicked {
    private String campaignId;
    private String buttonId;
    private long timestamp;

    public ButtonClicked(String campaignId, String buttonId, long timestamp) {
        this.campaignId = campaignId;
        this.buttonId = buttonId;
        this.timestamp = timestamp;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getButtonId() {
        return buttonId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ButtonClicked that = (ButtonClicked) o;

        if (timestamp != that.timestamp) return false;
        if (campaignId != null ? !campaignId.equals(that.campaignId) : that.campaignId != null)
            return false;
        return buttonId != null ? buttonId.equals(that.buttonId) : that.buttonId == null;
    }

    @Override
    public int hashCode() {
        int result = campaignId != null ? campaignId.hashCode() : 0;
        result = 31 * result + (buttonId != null ? buttonId.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ButtonClicked{" +
                "campaignId='" + campaignId + '\'' +
                ", buttonId='" + buttonId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
