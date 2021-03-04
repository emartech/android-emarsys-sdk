package com.emarsys.core.handler;

import android.os.Handler
import com.emarsys.core.Mockable

@Mockable
class CoreSdkHandler(final val handler: Handler) {
    fun post(runnable: Runnable) {
        handler.post(runnable)
    }

    val looper = handler.looper
}