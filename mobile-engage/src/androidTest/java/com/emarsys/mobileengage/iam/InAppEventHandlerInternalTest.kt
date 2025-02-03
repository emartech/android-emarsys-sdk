package com.emarsys.mobileengage.iam


import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test

class InAppEventHandlerInternalTest  {

    private lateinit var inAppEventHandlerInternal: InAppEventHandlerInternal


    @Before
    fun setUp() {
        inAppEventHandlerInternal = InAppEventHandlerInternal()
    }

    @Test
    fun testIsPaused_returnsFalse_byDefault() {
        inAppEventHandlerInternal.isPaused shouldBe false
    }

    @Test
    fun testPause_setsIsPaused_toTrue() {
        inAppEventHandlerInternal.pause()

        inAppEventHandlerInternal.isPaused shouldBe true
    }

    @Test
    fun testResume_setsIsPaused_toFalse() {
        inAppEventHandlerInternal.pause()
        inAppEventHandlerInternal.resume()

        inAppEventHandlerInternal.isPaused shouldBe false
    }
}