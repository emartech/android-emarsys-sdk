package com.emarsys.mobileengage.inbox.model;

import com.emarsys.mobileengage.api.inbox.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationCache {

    static List<Notification> internalCache = new ArrayList<>();

    public void cache(Notification notification) {
        if (notification != null) {
            internalCache.add(0, notification);
        }
    }

    public List<Notification> merge(List<Notification> fetchedList) {
        invalidate(fetchedList);

        ArrayList<Notification> result = new ArrayList<>(internalCache);
        result.addAll(fetchedList);
        return result;
    }

    public void invalidate(List<Notification> fetchedNotifications) {
        for (int i = internalCache.size() - 1; i >= 0; --i) {
            Notification cached = internalCache.get(i);
            for (Notification fetched: fetchedNotifications) {
                if (fetched.getId().equals(cached.getId())) {
                    internalCache.remove(i);
                    break;
                }
            }
        }
    }
}
