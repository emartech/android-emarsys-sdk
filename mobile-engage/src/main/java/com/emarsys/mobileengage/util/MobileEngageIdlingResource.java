package com.emarsys.mobileengage.util;

import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;

public class MobileEngageIdlingResource implements IdlingResource {
    CountingIdlingResource delegateIdlingResource;

    public MobileEngageIdlingResource(String resourceName) {
        this.delegateIdlingResource = new CountingIdlingResource(resourceName, true);
    }

    @Override
    public String getName() {
        return delegateIdlingResource.getName();
    }

    @Override
    public boolean isIdleNow() {
        return delegateIdlingResource.isIdleNow();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        delegateIdlingResource.registerIdleTransitionCallback(callback);
    }

    public void increment() {
        delegateIdlingResource.increment();
    }

    public void decrement() {
        delegateIdlingResource.decrement();
    }
}
