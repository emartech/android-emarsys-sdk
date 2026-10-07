package com.emarsys.core.fake

import android.os.Looper
import com.emarsys.core.request.factory.RunnableFactory
import java.util.concurrent.CountDownLatch

class FakeRunnableFactory(
    var latch: CountDownLatch,
    var checkNonUIThread: Boolean = false
) : RunnableFactory {

    var executionCount = 0

    override fun runnableFrom(runnable: Runnable): Runnable = Runnable {
        runnable.run()
        if (checkNonUIThread) {
            if (isCoreSDKHandlerThread()) executionCount++
        } else {
            executionCount++
        }
        latch.countDown()
    }

    private fun isCoreSDKHandlerThread(): Boolean =
        Looper.myLooper() != null &&
            Looper.myLooper()!!.thread.name.startsWith("CoreSDKHandlerThread")
}
