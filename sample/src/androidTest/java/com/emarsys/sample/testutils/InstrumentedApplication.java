package com.emarsys.sample.testutils;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.emarsys.sample.SampleApplication;

public class InstrumentedApplication extends SampleApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
