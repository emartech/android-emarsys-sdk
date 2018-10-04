package com.emarsys.core.provider.activity;

import android.app.Activity;

import com.emarsys.core.provider.Property;

import java.lang.ref.WeakReference;

public class CurrentActivityProvider implements Property<Activity> {

    private WeakReference<Activity> activityWeakReference;

    public CurrentActivityProvider() {
        activityWeakReference = new WeakReference<>(null);
    }

    @Override
    public Activity get() {
        return activityWeakReference.get();
    }

    @Override
    public void set(Activity value) {
        this.activityWeakReference = new WeakReference<>(value);
    }

}
