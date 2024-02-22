package com.emarsys.core.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ExceptionExtensionsKtTest {

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