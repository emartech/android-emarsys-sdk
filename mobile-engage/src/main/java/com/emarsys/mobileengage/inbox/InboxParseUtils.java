package com.emarsys.mobileengage.inbox;

import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InboxParseUtils {
    private InboxParseUtils() {
    }

    public static NotificationInboxStatus parseNotificationInboxStatus(String jsonString) {
        NotificationInboxStatus result = new NotificationInboxStatus();
        if (jsonString != null) {
            try {
                JSONObject json = new JSONObject(jsonString);

                List<Notification> notifications = null;
                if (json.has("notifications")) {
                    notifications = parseNotificationList(json.getString("notifications"));
                }

                int badgeCount = parseBadgeCount(jsonString);

                result = new NotificationInboxStatus(notifications, badgeCount);
            } catch (JSONException e) {
            }
        }
        return result;
    }

    public static int parseBadgeCount(String jsonString) {
        int result = 0;
        if (jsonString != null) {
            try {
                JSONObject json = new JSONObject(jsonString);
                result = json.getInt("badge_count");
            } catch (JSONException e) {
            }
        }
        return result;
    }

    public static List<Notification> parseNotificationList(String jsonString) {
        List<Notification> result = new ArrayList<>();
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i < array.length(); i++) {
                    String notificationString = array.getString(i);
                    Notification notification = parseNotification(notificationString);
                    if (notification != null) {
                        result.add(notification);
                    }
                }
            } catch (JSONException e) {
            }
        }
        return result;
    }

    public static Notification parseNotification(String jsonString) {
        Notification result = null;
        if (jsonString != null) {
            try {
                JSONObject json = new JSONObject(jsonString);
                String id = json.getString("id");
                String sid = json.getString("sid");
                String title = json.getString("title");
                String body = null;
                if (json.has("body")) {
                    body = json.getString("body");
                }
                Map<String, String> customData = convertFlatJsonObject(new JSONObject(json.getString("custom_data")));
                JSONObject rootParams = new JSONObject(json.getString("root_params"));
                int expirationTime = json.getInt("expiration_time");
                long receivedAt = json.getLong("received_at");
                result = new Notification(id, sid, title, body, customData, rootParams, expirationTime, receivedAt);
            } catch (JSONException e) {
            }
        }
        return result;
    }

    public static Notification parseNotificationFromPushMessage(Map<String, String> remoteData, boolean isUserCentric) {
        Notification result = null;
        if (remoteData != null && "true".equals(remoteData.get("inbox"))) {

            String id;
            if (isUserCentric) {
                id = remoteData.get("message_id");
            } else {
                id = remoteData.get("id");
            }

            String sid = null;
            try {
                sid = new JSONObject(remoteData.get("u")).getString("sid");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String title = remoteData.get("title");
            String body = remoteData.get("body");

            Map<String, String> customData = null;
            try {
                customData = convertFlatJsonObject(new JSONObject(remoteData.get("u")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject rootParams = new JSONObject();

            int expirationTime = Integer.MAX_VALUE;

            long receivedAt = System.currentTimeMillis();

            result = new Notification(id, sid, title, body, customData, rootParams, expirationTime, receivedAt);
        }
        return result;
    }

    public static Map<String, String> convertFlatJsonObject(JSONObject jsonObject) {
        Map<String, String> result = new HashMap<>();
        if (jsonObject != null) {
            try {
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    result.put(key, jsonObject.getString(key));
                }
            } catch (JSONException e) {
            }
        }
        return result;
    }
}
