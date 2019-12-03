package com.emarsys.fake

import android.os.Looper
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import java.util.concurrent.CountDownLatch

class FakeResultListener<T> @JvmOverloads constructor(var latch: CountDownLatch?, var mode: Mode = Mode.ALL_THREAD) : ResultListener<Try<T>> {
    enum class Mode {
        MAIN_THREAD, ALL_THREAD
    }

    var successCount = 0
    var resultStatus: T? = null
    var errorCause: Throwable? = null
    var errorCount = 0
    override fun onResult(result: Try<T>) {
        if (result.result != null) {
            onSuccess(result.result)
        }
        if (result.errorCause != null) {
            onError(result.errorCause)
        }
    }

    private fun onSuccess(result: T?) {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleSuccess(result)
        } else if (mode == Mode.ALL_THREAD) {
            handleSuccess(result)
        }
    }

    private fun onError(cause: Throwable?) {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleError(cause)
        } else if (mode == Mode.ALL_THREAD) {
            handleError(cause)
        }
    }

    private fun handleSuccess(result: T?) {
        resultStatus = result
        successCount++
        if (latch != null) {
            latch!!.countDown()
        }
    }

    private fun handleError(cause: Throwable?) {
        errorCount++
        errorCause = cause
        if (latch != null) {
            latch!!.countDown()
        }
    }

    private fun onMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

}