package com.emarsys.core.notification;

public class ChannelSettings {
    private final String getChannelId;

    private final int getImportance;

    private final boolean canBypassDnd;

    private final boolean canShowBadge;

    private final boolean shouldVibrate;

    private final boolean shouldShowLights;

    public ChannelSettings(String getChannelId, int getImportance, boolean canBypassDnd, boolean canShowBadge, boolean shouldVibrate, boolean shouldShowLights) {
        this.getChannelId = getChannelId;
        this.getImportance = getImportance;
        this.canBypassDnd = canBypassDnd;
        this.canShowBadge = canShowBadge;
        this.shouldVibrate = shouldVibrate;
        this.shouldShowLights = shouldShowLights;
    }

    public String getChannelId() {
        return getChannelId;
    }

    public int getImportance() {
        return getImportance;
    }

    public boolean isCanBypassDnd() {
        return canBypassDnd;
    }

    public boolean isCanShowBadge() {
        return canShowBadge;
    }

    public boolean isShouldVibrate() {
        return shouldVibrate;
    }

    public boolean isShouldShowLights() {
        return shouldShowLights;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChannelSettings that = (ChannelSettings) o;

        if (getImportance != that.getImportance) return false;
        if (canBypassDnd != that.canBypassDnd) return false;
        if (canShowBadge != that.canShowBadge) return false;
        if (shouldVibrate != that.shouldVibrate) return false;
        if (shouldShowLights != that.shouldShowLights) return false;
        return getChannelId != null ? getChannelId.equals(that.getChannelId) : that.getChannelId == null;
    }

    @Override
    public int hashCode() {
        int result = getChannelId != null ? getChannelId.hashCode() : 0;
        result = 31 * result + getImportance;
        result = 31 * result + (canBypassDnd ? 1 : 0);
        result = 31 * result + (canShowBadge ? 1 : 0);
        result = 31 * result + (shouldVibrate ? 1 : 0);
        result = 31 * result + (shouldShowLights ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChannelSettings{" +
                "getChannelId='" + getChannelId + '\'' +
                ", getImportance=" + getImportance +
                ", canBypassDnd=" + canBypassDnd +
                ", canShowBadge=" + canShowBadge +
                ", shouldVibrate=" + shouldVibrate +
                ", shouldShowLights=" + shouldShowLights +
                '}';
    }
}
