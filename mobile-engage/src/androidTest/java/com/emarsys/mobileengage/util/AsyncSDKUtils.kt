package com.emarsys.mobileengage.util

import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import java.util.concurrent.CountDownLatch

fun waitForTask() {
    val latch = CountDownLatch(2)
    DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler().post {
        latch.countDown()
        DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler().post {
            latch.countDown()
        }
    }
    latch.await()
}