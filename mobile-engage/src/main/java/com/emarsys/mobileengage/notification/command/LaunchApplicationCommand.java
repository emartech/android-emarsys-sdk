package com.emarsys.mobileengage.notification.command;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.CrashLog;
import com.emarsys.mobileengage.notification.LaunchActivityCommandLifecycleCallbacksFactory;
import com.emarsys.mobileengage.service.IntentUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LaunchApplicationCommand implements Runnable {

    Intent intent;
    Context context;
    LaunchActivityCommandLifecycleCallbacksFactory provider;

    public LaunchApplicationCommand(Intent intent, Context context, LaunchActivityCommandLifecycleCallbacksFactory provider) {
        Assert.notNull(intent, "Intent must not be null!");
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(provider, "LifecycleCallbackProvider must not be null!");
        this.intent = intent;
        this.context = context;
        this.provider = provider;
    }

    @Override
    public void run() {
        final Intent launchIntent = IntentUtils.createLaunchIntent(intent, context);
        final CountDownLatch latch = new CountDownLatch(1);
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(provider.create(latch));

        if (launchIntent != null) {
            context.startActivity(launchIntent);
        }
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.error(new CrashLog(e, null));
        }
    }
}
