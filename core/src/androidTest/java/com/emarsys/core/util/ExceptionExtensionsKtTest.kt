package com.emarsys.core.util

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe

class ExceptionExtensionsKtTest : AnnotationSpec() {

    @Test
    fun testRootCause_shouldReturn_withTheRootCauseOfTheException() {
        val expectation = Throwable("root cause")
        val testException: Exception = Exception(
            Throwable(
                Throwable(
                    "root cause", Throwable(
                        Throwable(Throwable(expectation))
                    )
                )
            )
        )

        val result = testException.rootCause()

        result shouldBe expectation
    }
}