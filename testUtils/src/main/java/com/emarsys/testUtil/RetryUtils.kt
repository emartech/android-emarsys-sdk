package com.emarsys.testUtil

import com.emarsys.testUtil.rules.RetryRule

object RetryUtils {
    @JvmStatic
    val retryRule
        get() = RetryRule(3)
}