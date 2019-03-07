package com.emarsys.mobileengage.fake

import com.emarsys.core.api.result.CompletionListener
import java.util.concurrent.CountDownLatch

open class FakeCompletionListener(private val countDownLatch: CountDownLatch, private val completionListener: CompletionListener) : CompletionListener {
    override fun onCompleted(errorCause: Throwable?) {
        countDownLatch.countDown()
        completionListener.onCompleted(errorCause)
    }
}
