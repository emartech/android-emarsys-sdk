package com.emarsys.mobileengage.notification.command;

import android.content.Context;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.api.NotificationEventHandler;

import org.json.JSONObject;

public class AppEventCommand implements Runnable {

    private String name;
    private JSONObject payload;
    private Context context;
    private NotificationEventHandler notificationEventHandler;

    public AppEventCommand(Context context, NotificationEventHandler notificationEventHandler, String name, JSONObject payload) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(name, "Name must not be null!");
        this.context = context;
        this.notificationEventHandler = notificationEventHandler;
        this.name = name;
        this.payload = payload;
    }

    public Context getContext() {
        return context;
    }

    public String getName() {
        return name;
    }

    public JSONObject getPayload() {
        return payload;
    }

    public NotificationEventHandler getNotificationEventHandler() {
        return notificationEventHandler;
    }

    @Override
    public void run() {
        if (notificationEventHandler != null) {
            notificationEventHandler.handleEvent(context, name, payload);
        }
    }
}
