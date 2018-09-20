package com.emarsys.testUtil;

import com.emarsys.core.experimental.ExperimentalFeatures;

public class ExperimentalTestUtils {

    private ExperimentalTestUtils() {
    }

    public static void resetExperimentalFeatures() {
        ReflectionTestUtils.invokeStaticMethod(ExperimentalFeatures.class, "reset");
    }
}
