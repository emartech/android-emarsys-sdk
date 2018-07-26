package com.emarsys.mobileengage.inbox.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NotificationInboxStatus {
    private final List<Notification> notifications;
    private final int badgeCount;

    public NotificationInboxStatus() {
        this(new ArrayList<Notification>(), 0);
    }

    public NotificationInboxStatus(List<Notification> notifications, int badgeCount) {
        this.notifications = notifications == null ? new ArrayList<Notification>() : notifications;
        this.badgeCount = badgeCount;
    }

    @NonNull
    public List<Notification> getNotifications() {
        return notifications;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationInboxStatus that = (NotificationInboxStatus) o;

        if (badgeCount != that.badgeCount) return false;
        return notifications != null ? notifications.equals(that.notifications) : that.notifications == null;

    }

    @Override
    public int hashCode() {
        int result = notifications != null ? notifications.hashCode() : 0;
        result = 31 * result + badgeCount;
        return result;
    }

    @Override
    public String toString() {
        return "NotificationInboxStatus{" +
                "notifications=" + notifications +
                ", badgeCount=" + badgeCount +
                '}';
    }
}
