package com.emarsys.mobileengage.notification.command;

import android.content.Intent;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.push.PushInternal;

public class TrackMessageOpenCommand implements Runnable {

    private final PushInternal pushInternal;
    private final Intent intent;

    public TrackMessageOpenCommand(PushInternal pushInternal, Intent intent) {
        Assert.notNull(pushInternal, "PushInternal must not be null!");
        Assert.notNull(intent, "Intent must not be null!");

        this.pushInternal = pushInternal;
        this.intent = intent;
    }

    @Override
    public void run() {
        pushInternal.trackMessageOpen(intent, null);
    }
}
