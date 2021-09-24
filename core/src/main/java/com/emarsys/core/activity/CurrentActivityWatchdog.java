package com.emarsys.core.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.emarsys.core.provider.Property;
import com.emarsys.core.util.Assert;


public class CurrentActivityWatchdog implements Application.ActivityLifecycleCallbacks {

    private Property<Activity> currentActivityProvider;

    public CurrentActivityWatchdog(Property<Activity> currentActivityProvider) {
        Assert.notNull(currentActivityProvider, "CurrentActivityProvider must not be null!");
        this.currentActivityProvider = currentActivityProvider;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivityProvider.set(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (currentActivityProvider.get() == activity) {
            currentActivityProvider.set(null);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
