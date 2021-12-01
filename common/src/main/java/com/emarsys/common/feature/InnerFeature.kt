package com.emarsys.common.feature

import com.emarsys.core.api.experimental.FlipperFeature
import java.util.*

enum class InnerFeature : FlipperFeature {
    MOBILE_ENGAGE, PREDICT, EVENT_SERVICE_V4, APP_EVENT_CACHE;

    override val featureName: String
        get() = "inner_feature_" + name.lowercase(Locale.getDefault())

    companion object {
        fun safeValueOf(enumAsString: String): InnerFeature? {
            return try {
                valueOf(enumAsString)
            } catch (ignored: IllegalArgumentException) {
                null
            }
        }
    }
}