package com.emarsys.inapp;

import androidx.annotation.NonNull;

import com.emarsys.core.Callable;
import com.emarsys.core.RunnerProxy;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.api.event.EventHandler;
import com.emarsys.mobileengage.iam.InAppInternal;

public class InAppProxy implements InAppApi {

    private final RunnerProxy runnerProxy;
    private final InAppInternal inAppInternal;

    public InAppProxy(RunnerProxy runnerProxy, InAppInternal inAppInternal) {
        Assert.notNull(runnerProxy, "RunnerProxy must not be null!");
        Assert.notNull(inAppInternal, "InAppInternal must not be null!");

        this.runnerProxy = runnerProxy;
        this.inAppInternal = inAppInternal;
    }

    @Override
    public void pause() {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                inAppInternal.pause();
            }
        });
    }

    @Override
    public void resume() {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                inAppInternal.resume();
            }
        });
    }

    @Override
    public boolean isPaused() {
        return runnerProxy.logException(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return inAppInternal.isPaused();
            }
        });
    }

    @Override
    public void setEventHandler(@NonNull final EventHandler eventHandler) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(eventHandler, "EventHandler must not be null!");

                inAppInternal.setEventHandler(eventHandler);
            }
        });
    }

}
