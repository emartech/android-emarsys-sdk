package com.emarsys.mobileengage.notification.command;

import android.content.Context;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.api.event.EventHandler;
import com.emarsys.mobileengage.event.EventHandlerProvider;

import org.json.JSONObject;

public class AppEventCommand implements Runnable {

    private String name;
    private JSONObject payload;
    private Context context;
    private EventHandlerProvider eventHandlerProvider;

    public AppEventCommand(Context context, EventHandlerProvider eventHandlerProvider, String name, JSONObject payload) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(eventHandlerProvider, "EventHandlerProvider must not be null!");
        this.context = context;
        this.eventHandlerProvider = eventHandlerProvider;
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

    public EventHandler getNotificationEventHandler() {
        return eventHandlerProvider.getEventHandler();
    }

    @Override
    public void run() {
        EventHandler eventHandler = eventHandlerProvider.getEventHandler();
        if (eventHandler != null) {
            eventHandler.handleEvent(context, name, payload);
        }
    }
}
