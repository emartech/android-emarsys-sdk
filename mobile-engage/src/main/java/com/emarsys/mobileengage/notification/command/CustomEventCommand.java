package com.emarsys.mobileengage.notification.command;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.event.EventServiceInternal;

import java.util.Map;

public class CustomEventCommand implements Runnable {

    private final EventServiceInternal eventServiceInternal;
    private final String eventName;
    private final Map<String, String> eventAttributes;
    private final Long triggerTimestamp;

    public CustomEventCommand(EventServiceInternal eventServiceInternal, String eventName, Map<String, String> eventAttributes) {
        this(eventServiceInternal, eventName, eventAttributes, null);
    }

    public CustomEventCommand(EventServiceInternal eventServiceInternal, String eventName, Map<String, String> eventAttributes, Long triggerTimestamp) {
        Assert.notNull(eventServiceInternal, "EventServiceInternal must not be null!");
        Assert.notNull(eventName, "EventName must not be null!");
        this.eventServiceInternal = eventServiceInternal;
        this.eventName = eventName;
        this.eventAttributes = eventAttributes;
        this.triggerTimestamp = triggerTimestamp;
    }

    @Override
    public void run() {
        if (triggerTimestamp != null) {
            eventServiceInternal.trackCustomEventAsync(eventName, eventAttributes, null, triggerTimestamp);
        } else {
            eventServiceInternal.trackCustomEventAsync(eventName, eventAttributes, null);
        }
    }

    public String getEventName() {
        return eventName;
    }

    public Map<String, String> getEventAttributes() {
        return eventAttributes;
    }
}
