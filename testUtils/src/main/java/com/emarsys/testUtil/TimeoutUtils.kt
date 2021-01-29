package com.emarsys.testUtil

import androidx.test.rule.DisableOnAndroidDebug

import org.junit.rules.Timeout

object TimeoutUtils {

    @JvmStatic
    val timeoutRule
        get () = DisableOnAndroidDebug(Timeout.seconds(60))

    @JvmStatic
    val longTimeoutRule
        get () = DisableOnAndroidDebug(Timeout.seconds(120))

}
