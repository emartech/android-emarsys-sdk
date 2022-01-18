package com.emarsys.core.handler

import android.os.Handler
import com.emarsys.core.Mockable

@Mockable
class SdkHandler(val handler: Handler) {
    fun post(runnable: Runnable): Boolean {
        return handler.post(runnable)
    }

    fun postDelayed(runnable: Runnable, delay: Long) {
        handler.postDelayed(runnable, delay)
    }

}