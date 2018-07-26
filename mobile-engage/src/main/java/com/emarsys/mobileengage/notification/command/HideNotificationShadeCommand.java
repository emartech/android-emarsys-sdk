package com.emarsys.mobileengage.notification.command;

import android.content.Context;
import android.content.Intent;

import com.emarsys.core.util.Assert;

public class HideNotificationShadeCommand implements Runnable {

    private final Context context;

    public HideNotificationShadeCommand(Context context) {
        Assert.notNull(context, "Context must not be null!");
        this.context = context;
    }

    @Override
    public void run() {
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }
}
