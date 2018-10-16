package com.emarsys.testUtil

import com.emarsys.core.experimental.ExperimentalFeatures

object ExperimentalTestUtils {

    @JvmStatic
    fun resetExperimentalFeatures() {
        ReflectionTestUtils.invokeStaticMethod(ExperimentalFeatures::class.java, "reset")
    }
}
