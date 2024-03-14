package com.emarsys.testUtil

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch

object ExtensionTestUtils {
    inline fun <reified T> Any.tryCast(block: T.() -> Unit) {
        if (this is T) {
            block()
        } else {
            throw IllegalArgumentException("Casted value is not the type of ${T::class.java.name}")
        }
    }

    fun <T> Any.runOnMain(logic: () -> T): T {
        val uiHandler = Handler(Looper.getMainLooper())
        return if (Thread.currentThread() != uiHandler.looper.thread) {
            var result: T? = null
            val latch = CountDownLatch(1)
            uiHandler.post {
                result = logic()
                latch.countDown()
            }
            latch.await()
            result!!
        } else {
            logic()
        }
    }
}