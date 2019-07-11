package com.emarsys.mobileengage.notification.command;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.event.EventServiceInternal;

import java.util.HashMap;
import java.util.Map;

public class TrackActionClickCommand implements Runnable {

    private final EventServiceInternal eventServiceInternal;
    private final String buttonId;
    private final String sid;

    public TrackActionClickCommand(EventServiceInternal eventServiceInternal, String buttonId, String sid) {
        Assert.notNull(eventServiceInternal, "EventServiceInternal must not be null!");
        Assert.notNull(buttonId, "ButtonId must not be null!");
        Assert.notNull(sid, "Sid must not be null!");
        this.eventServiceInternal = eventServiceInternal;
        this.buttonId = buttonId;
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    @Override
    public void run() {
        Map<String, String> payload = new HashMap<>();
        payload.put("button_id", buttonId);
        payload.put("origin", "button");
        payload.put("sid", sid);

        eventServiceInternal.trackInternalCustomEvent("push:click", payload, null);
    }

}
