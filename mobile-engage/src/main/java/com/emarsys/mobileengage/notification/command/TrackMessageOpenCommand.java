package com.emarsys.mobileengage.notification.command;

import android.content.Intent;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;

public class TrackMessageOpenCommand implements Runnable {

    private final MobileEngageInternal mobileEngageInternal;
    private final Intent intent;

    public TrackMessageOpenCommand(MobileEngageInternal mobileEngageInternal, Intent intent) {
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        Assert.notNull(intent, "Intent must not be null!");

        this.mobileEngageInternal = mobileEngageInternal;
        this.intent = intent;
    }

    @Override
    public void run() {
        mobileEngageInternal.trackMessageOpen(intent, null);
    }
}
