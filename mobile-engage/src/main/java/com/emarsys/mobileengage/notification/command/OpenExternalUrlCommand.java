package com.emarsys.mobileengage.notification.command;

import android.content.Context;
import android.content.Intent;

import com.emarsys.core.util.Assert;

public class OpenExternalUrlCommand implements Runnable {

    private final Context context;
    private final Intent intent;

    public OpenExternalUrlCommand(Intent intent, Context context) {
        Assert.notNull(intent, "Intent must not be null!");
        Assert.notNull(context, "Context must not be null!");
        this.intent = intent;
        this.context = context;
    }

    @Override
    public void run() {
        context.startActivity(intent);
    }

    public Context getContext() {
        return context;
    }

    public Intent getIntent() {
        return intent;
    }
}
