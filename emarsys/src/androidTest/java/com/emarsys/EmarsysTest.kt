package com.emarsys

import com.emarsys.testUtil.TimeoutUtils
import org.junit.Rule
import org.junit.Test

class EmarsysTest {

    @Rule
    @JvmField
    val timeoutRule = TimeoutUtils.timeoutRule

    @Test(expected = IllegalArgumentException::class)
    fun testTrackCustomEvent_eventNameMustNotBeNull() {
        Emarsys.trackCustomEvent(null, emptyMap())
    }
}
