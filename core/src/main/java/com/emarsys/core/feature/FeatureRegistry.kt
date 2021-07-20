package com.emarsys.core.feature

import com.emarsys.core.api.experimental.FlipperFeature
import java.util.*

object FeatureRegistry {
    @JvmField
    var enabledFeatures: MutableSet<String> = HashSet()

    @JvmStatic
    fun isFeatureEnabled(feature: FlipperFeature): Boolean {
        return enabledFeatures.contains(feature.featureName)
    }

    @JvmStatic
    fun enableFeature(feature: FlipperFeature) {
        enabledFeatures.add(feature.featureName)
    }

    @JvmStatic
    fun reset() {
        enabledFeatures.clear()
    }

    @JvmStatic
    fun disableFeature(feature: FlipperFeature) {
        enabledFeatures.remove(feature.featureName)
    }
}