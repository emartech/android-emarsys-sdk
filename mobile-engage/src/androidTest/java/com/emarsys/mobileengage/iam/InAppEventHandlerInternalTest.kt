package com.emarsys.mobileengage.iam


import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe


class InAppEventHandlerInternalTest : AnnotationSpec() {

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