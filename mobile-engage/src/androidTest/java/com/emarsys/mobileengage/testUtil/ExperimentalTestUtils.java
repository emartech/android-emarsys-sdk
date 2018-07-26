package com.emarsys.mobileengage.testUtil;

import com.emarsys.mobileengage.experimental.MobileEngageExperimental;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExperimentalTestUtils {

    private ExperimentalTestUtils() {
    }

    public static void resetExperimentalFeatures() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method reset = MobileEngageExperimental.class.getDeclaredMethod("reset");
        reset.setAccessible(true);
        reset.invoke(null);
    }
}
