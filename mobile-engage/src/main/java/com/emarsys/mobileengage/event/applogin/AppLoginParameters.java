package com.emarsys.mobileengage.event.applogin;

public class AppLoginParameters {
    private int contactFieldId = -1;
    private String contactFieldValue;

    public AppLoginParameters() {
    }

    public AppLoginParameters(int contactFieldId, String contactFieldValue) {
        this.contactFieldId = contactFieldId;
        this.contactFieldValue = contactFieldValue;
    }

    public int getContactFieldId() {
        return contactFieldId;
    }

    public String getContactFieldValue() {
        return contactFieldValue;
    }

    public boolean hasCredentials() {
        return contactFieldId != -1 && contactFieldValue != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppLoginParameters that = (AppLoginParameters) o;

        if (contactFieldId != that.contactFieldId) return false;
        return contactFieldValue != null ? contactFieldValue.equals(that.contactFieldValue) : that.contactFieldValue == null;

    }

    @Override
    public int hashCode() {
        int result = contactFieldId;
        result = 31 * result + (contactFieldValue != null ? contactFieldValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AppLoginParameters{" +
                "contactFieldId=" + contactFieldId +
                ", contactFieldValue='" + contactFieldValue + '\'' +
                '}';
    }
}
