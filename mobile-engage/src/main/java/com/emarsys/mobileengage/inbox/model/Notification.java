package com.emarsys.mobileengage.inbox.model;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.Map;

public class Notification {
    private final String id;
    private final String sid;
    private final String title;
    private final String body;
    private final Map<String, String> customData;
    private final JSONObject rootParams;
    private final int expirationTime;
    private final long receivedAt;

    public Notification(String id, String sid, String title, String body, Map<String, String> customData, JSONObject rootParams, int expirationTime, long receivedAt) {
        this.id = id;
        this.sid = sid;
        this.title = title;
        this.body = body;
        this.customData = customData;
        this.rootParams = rootParams;
        this.expirationTime = expirationTime;
        this.receivedAt = receivedAt;
    }

    public String getId() {
        return id;
    }

    public String getSid() {
        return sid;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public Map<String, String> getCustomData() {
        return customData;
    }

    @NonNull
    public JSONObject getRootParams() {
        return rootParams;
    }

    @NonNull
    public int getExpirationTime() {
        return expirationTime;
    }

    @NonNull
    public long getReceivedAt() {
        return receivedAt;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Notification that = (Notification) o;

        if (expirationTime != that.expirationTime) return false;
        if (receivedAt != that.receivedAt) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (sid != null ? !sid.equals(that.sid) : that.sid != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (customData != null ? !customData.equals(that.customData) : that.customData != null)
            return false;
        if (rootParams != null ? !rootParams.toString().equals(that.rootParams.toString()) : that.rootParams != null)
            return false;
        return receivedAt == that.receivedAt;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (sid != null ? sid.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (customData != null ? customData.hashCode() : 0);
        result = 31 * result + (rootParams != null ? rootParams.hashCode() : 0);
        result = 31 * result + expirationTime;
        result = 31 * result + (int) (receivedAt ^ (receivedAt >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", sid='" + sid + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", customData=" + customData +
                ", rootParams=" + rootParams +
                ", expirationTime=" + expirationTime +
                ", receivedAt=" + receivedAt +
                '}';
    }
}
