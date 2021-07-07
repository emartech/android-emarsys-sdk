package com.emarsys.common.feature

import com.emarsys.core.api.experimental.FlipperFeature
import java.util.*

enum class InnerFeature : FlipperFeature {
    MOBILE_ENGAGE, PREDICT, EVENT_SERVICE_V4;

    override fun getName(): String {
        return "inner_feature_" + name.lowercase(Locale.getDefault())
    }

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