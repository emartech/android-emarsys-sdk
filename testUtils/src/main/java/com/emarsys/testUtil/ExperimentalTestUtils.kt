package com.emarsys.testUtil

object ExperimentalTestUtils {

    @JvmStatic
    fun resetExperimentalFeatures() {
        ReflectionTestUtils.invokeStaticMethod(Class.forName("com.emarsys.core.experimental.ExperimentalFeatures"), "reset")
    }
}
