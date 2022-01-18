package com.emarsys.core.concurrency

import android.os.Handler
import android.os.HandlerThread
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.handler.SdkHandler
import java.util.*

class ConcurrentHandlerHolderFactory(private val uiHandler: Handler) {
    fun create(): ConcurrentHandlerHolder {
        val handlerThread = HandlerThread("CoreSDKHandlerThread-" + UUID.randomUUID().toString())
        handlerThread.start()
        return ConcurrentHandlerHolder(
            SdkHandler(CoreHandler(handlerThread)),
            SdkHandler(uiHandler)
        )
    }
}