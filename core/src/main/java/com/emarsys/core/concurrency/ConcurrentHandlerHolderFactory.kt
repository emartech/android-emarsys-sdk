package com.emarsys.core.concurrency

import android.os.HandlerThread
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.handler.SdkHandler
import java.util.*

object ConcurrentHandlerHolderFactory {
    fun create(): ConcurrentHandlerHolder {
        val coreHandlerThread = HandlerThread("CoreSDKHandlerThread-" + UUID.randomUUID().toString())
        coreHandlerThread.start()
        val networkHandlerThread = HandlerThread("NetworkHandlerThread-" + UUID.randomUUID().toString())
        networkHandlerThread.start()
        val backgroundHandlerThread = HandlerThread("NetworkHandlerThread-" + UUID.randomUUID().toString())
        backgroundHandlerThread.start()
        return ConcurrentHandlerHolder(
            SdkHandler(CoreHandler(coreHandlerThread)),
            SdkHandler(CoreHandler(networkHandlerThread)),
            SdkHandler(CoreHandler(backgroundHandlerThread))
        )
    }
}