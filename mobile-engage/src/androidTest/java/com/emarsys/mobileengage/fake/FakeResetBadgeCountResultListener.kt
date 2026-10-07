package com.emarsys.mobileengage.fake

import android.os.Looper
import com.emarsys.core.api.result.CompletionListener
import java.util.concurrent.CountDownLatch

class FakeResetBadgeCountResultListener(
    var latch: CountDownLatch?,
    var mode: Mode = Mode.ALL_THREAD
) : CompletionListener {

    enum class Mode { MAIN_THREAD, ALL_THREAD }

    var successCount = 0
    var errorCause: Throwable? = null
    var errorCount = 0

    override fun onCompleted(errorCause: Throwable?) {
        if (errorCause != null) onError(errorCause) else onSuccess()
    }

    private fun onSuccess() {
        if (mode == Mode.MAIN_THREAD && onMainThread()) handleSuccess()
        else if (mode == Mode.ALL_THREAD) handleSuccess()
    }

    private fun onError(cause: Throwable) {
        if (mode == Mode.MAIN_THREAD && onMainThread()) handleError(cause)
        else if (mode == Mode.ALL_THREAD) handleError(cause)
    }

    private fun handleSuccess() {
        successCount++
        latch?.countDown()
    }

    private fun handleError(cause: Throwable) {
        errorCount++
        errorCause = cause
        latch?.countDown()
    }

    private fun onMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()
}
