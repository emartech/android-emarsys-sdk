package com.emarsys.mobileengage.fake

import android.os.Looper
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import java.util.concurrent.CountDownLatch

class FakeMessageLoadedListener(
    var latch: CountDownLatch,
    var mode: Mode = Mode.ALL_THREAD
) : MessageLoadedListener {

    enum class Mode { MAIN_THREAD, ALL_THREAD }

    var invocationCount = 0

    override fun onMessageLoaded() {
        if (mode == Mode.MAIN_THREAD && onMainThread()) {
            handleLoaded()
        } else if (mode == Mode.ALL_THREAD) {
            handleLoaded()
        }
    }

    private fun handleLoaded() {
        invocationCount++
        latch.countDown()
    }

    private fun onMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()
}
