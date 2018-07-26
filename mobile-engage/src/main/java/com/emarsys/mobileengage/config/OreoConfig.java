package com.emarsys.mobileengage.config;

public class OreoConfig {

    public static final String DEFAULT_CHANNEL_ID = "ems_me_default";

    private final boolean isDefaultChannelEnabled;
    private final String defaultChannelName;
    private final String defaultChannelDescription;

    public OreoConfig(boolean isDefaultChannelEnabled) {
        this(isDefaultChannelEnabled, null, null);
    }

    public OreoConfig(boolean isDefaultChannelEnabled, String defaultChannelName, String defaultChannelDescription) {
        this.isDefaultChannelEnabled = isDefaultChannelEnabled;
        this.defaultChannelName = defaultChannelName;
        this.defaultChannelDescription = defaultChannelDescription;
    }

    public String getDefaultChannelDescription() {
        return defaultChannelDescription;
    }

    public boolean isDefaultChannelEnabled() {
        return isDefaultChannelEnabled;
    }

    public String getDefaultChannelName() {
        return defaultChannelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OreoConfig that = (OreoConfig) o;

        if (isDefaultChannelEnabled != that.isDefaultChannelEnabled) return false;
        if (defaultChannelName != null ? !defaultChannelName.equals(that.defaultChannelName) : that.defaultChannelName != null)
            return false;
        return defaultChannelDescription != null ? defaultChannelDescription.equals(that.defaultChannelDescription) : that.defaultChannelDescription == null;

    }

    @Override
    public int hashCode() {
        int result = (isDefaultChannelEnabled ? 1 : 0);
        result = 31 * result + (defaultChannelName != null ? defaultChannelName.hashCode() : 0);
        result = 31 * result + (defaultChannelDescription != null ? defaultChannelDescription.hashCode() : 0);
        return result;
    }
}
