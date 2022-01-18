package com.emarsys.core.handler;

import com.emarsys.core.Mockable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.asCoroutineDispatcher

@Mockable
class ConcurrentHandlerHolder(final val coreHandler: SdkHandler, final val uiHandler: SdkHandler) {
    fun post(runnable: Runnable) {
        coreHandler.post(runnable)
    }

    val sdkScope = CoroutineScope(Job() + this.coreHandler.handler.asCoroutineDispatcher())
    val uiScope = CoroutineScope(Job() + Dispatchers.Main)
    val looper = coreHandler.handler.looper
}