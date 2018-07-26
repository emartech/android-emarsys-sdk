package com.emarsys.mobileengage.notification.command;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;

import java.util.HashMap;
import java.util.Map;

public class TrackActionClickCommand implements Runnable {

    private final MobileEngageInternal mobileEngageInternal;
    private final String buttonId;
    private final String title;

    public TrackActionClickCommand(MobileEngageInternal mobileEngageInternal, String buttonId, String title) {
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        Assert.notNull(buttonId, "ButtonId must not be null!");
        Assert.notNull(title, "Title must not be null!");
        this.mobileEngageInternal = mobileEngageInternal;
        this.buttonId = buttonId;
        this.title = title;
    }

    @Override
    public void run() {
        if (MobileEngageExperimental.isV3Enabled()) {
            Map<String, String> payload = new HashMap<>();
            payload.put("button_id", buttonId);
            payload.put("title", title);
            mobileEngageInternal.trackInternalCustomEvent("richNotification:actionClicked", payload);
        }
    }

}
