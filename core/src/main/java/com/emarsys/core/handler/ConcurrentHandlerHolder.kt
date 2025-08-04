package com.emarsys.core.handler

import android.os.Handler
import android.os.Looper
import com.emarsys.core.Callable
import com.emarsys.core.Mockable
import java.util.concurrent.CountDownLatch

@Mockable
class ConcurrentHandlerHolder(
    val coreHandler: SdkHandler,
    val networkHandler: SdkHandler,
    val backgroundHandler: SdkHandler
) {

    val uiHandler = Handler(Looper.getMainLooper())
    val coreLooper = coreHandler.handler.looper
    val networkLooper = networkHandler.handler.looper
    val backgroundLooper = backgroundHandler.handler.looper

    fun post(runnable: Runnable) {
        if (coreHandler.handler.looper.thread.state != Thread.State.TERMINATED) {
            coreHandler.post(runnable)
        } else {
            postOnMain {
                coreHandler.post(runnable)
            }
        }
    }

    fun <T> run(callable: Callable<T>): T {
        var result: T? = null
        if (Thread.currentThread().name != coreHandler.handler.looper.thread.name) {
            val latch = CountDownLatch(1)
            post {
                result = callable.call()
                latch.countDown()
            }
            latch.await()
        } else {
            result = callable.call()
        }
        return result!!
    }

    fun postOnMain(runnable: Runnable) {
        uiHandler.post(runnable)
    }

    fun postOnNetwork(runnable: Runnable) {
        networkHandler.post(runnable)
    }

    fun postOnBackground(runnable: Runnable) {
        backgroundHandler.post(runnable)
    }
}