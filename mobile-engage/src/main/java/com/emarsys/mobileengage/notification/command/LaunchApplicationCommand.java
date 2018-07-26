package com.emarsys.mobileengage.notification.command;

import android.content.Context;
import android.content.Intent;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.service.IntentUtils;

public class LaunchApplicationCommand implements Runnable {

    Intent intent;
    Context context;

    public LaunchApplicationCommand(Intent intent, Context context) {
        Assert.notNull(intent, "Intent must not be null!");
        Assert.notNull(context, "Context must not be null!");
        this.intent = intent;
        this.context = context;
    }

    @Override
    public void run() {
        context.startActivity(IntentUtils.createLaunchIntent(intent, context));
    }

}
