package com.emarsys.mobileengage.notification.command;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;

import java.util.HashMap;
import java.util.Map;

public class TrackActionClickCommand implements Runnable {

    private final MobileEngageInternal mobileEngageInternal;
    private final String buttonId;
    private final String sid;

    public TrackActionClickCommand(MobileEngageInternal mobileEngageInternal, String buttonId, String sid) {
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        Assert.notNull(buttonId, "ButtonId must not be null!");
        Assert.notNull(sid, "Sid must not be null!");
        this.mobileEngageInternal = mobileEngageInternal;
        this.buttonId = buttonId;
        this.sid = sid;
    }

    @Override
    public void run() {
        Map<String, String> payload = new HashMap<>();
        payload.put("button_id", buttonId);
        payload.put("origin", "button");
        payload.put("sid", sid);

        mobileEngageInternal.trackInternalCustomEvent("push:click", payload, null);
    }

}
