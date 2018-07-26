package com.emarsys.mobileengage.notification.command;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngage;

import java.util.Map;

public class CustomEventCommand implements Runnable {

    private String eventName;
    private Map<String, String> eventAttributes;

    public CustomEventCommand(String eventName, Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
        this.eventName = eventName;
        this.eventAttributes = eventAttributes;
    }

    @Override
    public void run() {
        MobileEngage.trackCustomEvent(eventName, eventAttributes);
    }

    public String getEventName() {
        return eventName;
    }

    public Map<String, String> getEventAttributes() {
        return eventAttributes;
    }
}
