package com.emarsys.push;


import android.content.Intent;

import androidx.annotation.NonNull;

import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.push.PushInternal;

public class PushProxy implements PushApi {

    private final RunnerProxy runnerProxy;
    private final PushInternal pushInternal;

    public PushProxy(RunnerProxy runnerProxy, PushInternal pushInternal) {
        Assert.notNull(runnerProxy, "RunnerProxy must not be null!");
        Assert.notNull(pushInternal, "PushInternal must not be null!");
        this.runnerProxy = runnerProxy;
        this.pushInternal = pushInternal;
    }

    public void trackMessageOpen(@NonNull final Intent intent) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(intent, "Intent must not be null!");

                pushInternal.trackMessageOpen(intent, null);
            }
        });
    }

    public void trackMessageOpen(
            @NonNull final Intent intent,
            @NonNull final CompletionListener completionListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(intent, "Intent must not be null!");
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                pushInternal.trackMessageOpen(intent, completionListener);
            }
        });
    }

    public void setPushToken(@NonNull final String pushToken) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(pushToken, "PushToken must not be null!");

                pushInternal.setPushToken(pushToken, null);
            }
        });
    }

    public void setPushToken(
            @NonNull final String pushToken,
            @NonNull final CompletionListener completionListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(pushToken, "PushToken must not be null!");
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                pushInternal.setPushToken(pushToken, completionListener);
            }
        });
    }

    public void clearPushToken() {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                pushInternal.clearPushToken(null);
            }
        });
    }

    public void clearPushToken(final CompletionListener completionListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                pushInternal.clearPushToken(completionListener);
            }
        });
    }
}