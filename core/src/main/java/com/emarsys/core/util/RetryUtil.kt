package com.emarsys.core.util

object RetryUtil {

    fun retry(times: Int = 3, retryInterval: Long = 1000, action: () -> Unit) {
        try {
            action.invoke()
        } catch (e: Throwable) {
            if (times > 0) {
                Thread.sleep(retryInterval)
                retry(times - 1, retryInterval, action)
            } else {
                throw e
            }
        }
    }
}