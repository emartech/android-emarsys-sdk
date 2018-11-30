package com.emarsys.sample.testutils;

import android.content.Context;

import com.emarsys.sample.SampleApplication;

import androidx.multidex.MultiDex;

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
