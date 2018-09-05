package com.emarsys.mobileengage.testUtil;

import com.emarsys.core.experimental.ExperimentalFeatures;
import com.emarsys.mobileengage.experimental.MobileEngageExperimentalFeatures;
import com.emarsys.testUtil.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExperimentalTestUtils {

    private ExperimentalTestUtils() {
    }

    public static void resetExperimentalFeatures() {
        ReflectionTestUtils.invokeStaticMethod(ExperimentalFeatures.class, "reset");
    }
}
