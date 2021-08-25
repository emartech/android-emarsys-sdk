package com.emarsys.core.concurrency

import android.os.Handler
import com.emarsys.core.util.log.Logger.Companion.error
import android.os.HandlerThread
import android.os.Message
import com.emarsys.core.util.log.entry.CrashLog
import java.lang.Exception

class CoreHandler(handlerThread: HandlerThread) : Handler(handlerThread.looper) {
    override fun dispatchMessage(msg: Message) {
        try {
            super.dispatchMessage(msg)
        } catch (e: Exception) {
            error(CrashLog(e))
        }
    }
}