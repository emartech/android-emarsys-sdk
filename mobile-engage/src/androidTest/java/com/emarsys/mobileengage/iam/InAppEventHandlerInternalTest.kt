package com.emarsys.mobileengage.iam

import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InAppEventHandlerInternalTest {

    private lateinit var inAppEventHandlerInternal: InAppEventHandlerInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        inAppEventHandlerInternal = InAppEventHandlerInternal()
    }

    @Test
    fun testIsPaused_returnsFalse_byDefault() {
        assertFalse(inAppEventHandlerInternal.isPaused)
    }

    @Test
    fun testPause_setsIsPaused_toTrue() {
        inAppEventHandlerInternal.pause()

        assertTrue(inAppEventHandlerInternal.isPaused)
    }

    @Test
    fun testResume_setsIsPaused_toFalse() {
        inAppEventHandlerInternal.pause()
        inAppEventHandlerInternal.resume()

        assertFalse(inAppEventHandlerInternal.isPaused)
    }
}