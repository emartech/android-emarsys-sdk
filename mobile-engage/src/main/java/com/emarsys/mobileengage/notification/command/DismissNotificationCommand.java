package com.emarsys.mobileengage.notification.command;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.emarsys.core.util.Assert;

public class DismissNotificationCommand implements Runnable {

    private final Context context;
    private final Intent intent;

    public DismissNotificationCommand(Context context, Intent intent) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(intent, "Intent must not be null!");

        this.context = context;
        this.intent = intent;
    }

    @Override
    public void run() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Bundle bundle = intent.getBundleExtra("payload");
        if (bundle != null) {
            int notificationId = bundle.getInt("notification_id", Integer.MIN_VALUE);
            if (notificationId != Integer.MIN_VALUE) {
                manager.cancel(notificationId);
            }
        }
    }
}
