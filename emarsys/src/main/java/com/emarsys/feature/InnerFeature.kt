package com.emarsys.feature

import com.emarsys.core.api.experimental.FlipperFeature
import java.util.*

enum class InnerFeature : FlipperFeature {
    MOBILE_ENGAGE, PREDICT;

    override fun getName(): String {
        return "inner_feature_" + name.toLowerCase(Locale.getDefault())
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