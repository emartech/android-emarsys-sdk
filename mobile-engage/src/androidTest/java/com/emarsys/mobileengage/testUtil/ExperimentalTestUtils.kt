package com.emarsys.mobileengage.testUtil

import com.emarsys.core.experimental.ExperimentalFeatures
import com.emarsys.testUtil.ReflectionTestUtils

object ExperimentalTestUtils {

    fun resetExperimentalFeatures() {
        ReflectionTestUtils.invokeStaticMethod(ExperimentalFeatures::class.java, "reset")
    }
}
