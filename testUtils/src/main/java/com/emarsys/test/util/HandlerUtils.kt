package com.emarsys.test.util

import android.os.Handler
import java.util.concurrent.CountDownLatch

object HandlerUtils {

    @JvmStatic
    fun waitForEventLoopToFinish(handler: Handler) {
        val latch = CountDownLatch(1)
        handler.post { latch.countDown() }
        latch.await()
    }
}
