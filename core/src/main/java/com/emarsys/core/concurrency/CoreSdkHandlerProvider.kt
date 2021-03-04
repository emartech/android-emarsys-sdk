package com.emarsys.core.concurrency

import android.os.HandlerThread
import com.emarsys.core.handler.CoreSdkHandler
import java.util.*

class CoreSdkHandlerProvider {
    fun provideHandler(): CoreSdkHandler {
        val handlerThread = HandlerThread("CoreSDKHandlerThread-" + UUID.randomUUID().toString())
        handlerThread.start()
        return CoreSdkHandler(CoreHandler(handlerThread))
    }
}