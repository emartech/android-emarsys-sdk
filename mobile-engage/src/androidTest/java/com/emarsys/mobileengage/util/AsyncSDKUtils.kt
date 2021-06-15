package com.emarsys.mobileengage.util

import com.emarsys.mobileengage.di.mobileEngage

import java.util.concurrent.CountDownLatch

fun waitForTask() {
    val latch = CountDownLatch(2)
    mobileEngage().coreSdkHandler.post {
        latch.countDown()
        mobileEngage().coreSdkHandler.post {
            latch.countDown()
        }
    }
    latch.await()
}