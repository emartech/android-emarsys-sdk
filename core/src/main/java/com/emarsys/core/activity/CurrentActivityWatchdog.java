package com.emarsys.core.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;


public class CurrentActivityWatchdog implements Application.ActivityLifecycleCallbacks {

    static boolean isRegistered;
    static Activity currentActivity;

    public static void registerApplication(Application application) {
        if (!isRegistered) {
            application.registerActivityLifecycleCallbacks(new CurrentActivityWatchdog());
            isRegistered = true;
        }
    }

    public static Activity getCurrentActivity() {
        if (!isRegistered) {
            throw new IllegalStateException("The application must be registered before calling getCurrentActivity!");
        }
        return currentActivity;
    }

    static void reset() {
        isRegistered = false;
        currentActivity = null;
    }

    CurrentActivityWatchdog() {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
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
