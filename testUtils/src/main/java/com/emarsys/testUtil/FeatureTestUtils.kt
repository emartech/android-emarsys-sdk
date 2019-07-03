package com.emarsys.testUtil

object FeatureTestUtils {

    @JvmStatic
    fun resetFeatures() {
        ReflectionTestUtils.invokeStaticMethod(Class.forName("com.emarsys.core.feature.FeatureRegistry"), "reset")
    }
}
