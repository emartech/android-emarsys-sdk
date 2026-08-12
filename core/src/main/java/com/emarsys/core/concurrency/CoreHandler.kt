package com.emarsys.core.concurrency

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.emarsys.core.util.isNetworkException
import com.emarsys.core.util.log.Logger.Companion.error
import com.emarsys.core.util.log.entry.CrashLog

class CoreHandler(handlerThread: HandlerThread) : Handler(handlerThread.looper) {
    override fun dispatchMessage(msg: Message) {
        try {
            super.dispatchMessage(msg)
        } catch (e: Exception) {
            if (!e.isNetworkException()) {
                error(CrashLog(e))
            }
        }
    }
}