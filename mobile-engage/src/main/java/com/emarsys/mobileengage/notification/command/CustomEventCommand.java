package com.emarsys.mobileengage.notification.command;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;

import java.util.Map;

public class CustomEventCommand implements Runnable {

    private final MobileEngageInternal mobileEngageInternal;
    private String eventName;
    private Map<String, String> eventAttributes;

    public CustomEventCommand(MobileEngageInternal mobileEngageInternal, String eventName, Map<String, String> eventAttributes) {
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        Assert.notNull(eventName, "EventName must not be null!");
        this.mobileEngageInternal = mobileEngageInternal;
        this.eventName = eventName;
        this.eventAttributes = eventAttributes;
    }

    @Override
    public void run() {
        mobileEngageInternal.trackCustomEvent(eventName, eventAttributes, null);
    }

    public String getEventName() {
        return eventName;
    }

    public Map<String, String> getEventAttributes() {
        return eventAttributes;
    }
}
