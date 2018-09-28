package com.emarsys.core.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.emarsys.core.util.Assert;

import java.lang.ref.WeakReference;


public class CurrentActivityWatchdog implements Application.ActivityLifecycleCallbacks {

    private WeakReference<Activity> currentActivityWeakReference;

    public CurrentActivityWatchdog(Application application) {
        Assert.notNull(application, "Application must not be null!");
        application.registerActivityLifecycleCallbacks(this);
    }

    public Activity getCurrentActivity() {
        Activity activity = null;
        if (currentActivityWeakReference != null) {
            activity = currentActivityWeakReference.get();
        }
        return activity;
    }

    public void reset() {
        if (currentActivityWeakReference != null) {
            currentActivityWeakReference.clear();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (currentActivityWeakReference != null && currentActivityWeakReference.get() == activity) {
            currentActivityWeakReference.clear();
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
