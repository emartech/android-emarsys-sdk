package com.emarsys.mobileengage.testUtil;

import android.app.Activity;

import com.emarsys.core.activity.CurrentActivityWatchdog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CurrentActivityWatchdogTestUtils {

    public static void setActivityWatchdogState(Activity activity) throws NoSuchFieldException, IllegalAccessException {
        Field activityField = CurrentActivityWatchdog.class.getDeclaredField("currentActivity");
        activityField.setAccessible(true);
        activityField.set(null, activity);

        Field isRegisteredField = CurrentActivityWatchdog.class.getDeclaredField("isRegistered");
        isRegisteredField.setAccessible(true);
        isRegisteredField.set(null, true);
    }

    public static void resetCurrentActivityWatchdog() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = CurrentActivityWatchdog.class.getDeclaredMethod("reset");
        method.setAccessible(true);
        method.invoke(null);
    }
}
