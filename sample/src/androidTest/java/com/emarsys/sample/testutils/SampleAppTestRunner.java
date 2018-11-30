package com.emarsys.sample.testutils;

import android.app.Application;
import android.content.Context;
import androidx.test.runner.AndroidJUnitRunner;

public class SampleAppTestRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return super.newApplication(cl, InstrumentedApplication.class.getName(), context);
    }
}