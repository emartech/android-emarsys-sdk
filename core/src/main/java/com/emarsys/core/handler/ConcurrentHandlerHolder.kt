package com.emarsys.core.handler;

import android.os.Handler
import android.os.Looper
import com.emarsys.core.Mockable

@Mockable
class ConcurrentHandlerHolder(
    final val coreHandler: SdkHandler,
    final val networkHandler: SdkHandler,
    final val backgroundHandler: SdkHandler
) {

    val uiHandler = Handler(Looper.getMainLooper())
    val coreLooper = coreHandler.handler.looper
    val networkLooper = networkHandler.handler.looper
    val backgroundLooper = backgroundHandler.handler.looper

    fun post(runnable: Runnable) {
        coreHandler.post(runnable)
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