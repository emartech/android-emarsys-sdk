package com.emarsys.mobileengage.iam

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InAppEventHandlerInternalTest {

    private lateinit var inAppEventHandlerInternal: InAppEventHandlerInternal

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