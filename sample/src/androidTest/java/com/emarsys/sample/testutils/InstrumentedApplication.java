package com.emarsys.sample.testutils;

import android.os.Handler;

import com.emarsys.core.di.DependencyInjection;
import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.sample.SampleApplication;

import java.lang.reflect.Field;

public class InstrumentedApplication extends SampleApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Field handlerField = MobileEngage.class.getDeclaredField("coreSdkHandler");
            handlerField.setAccessible(true);
            Handler handler = (Handler) handlerField.get(null);
            handler.getLooper().quit();
            DependencyInjection.tearDown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
