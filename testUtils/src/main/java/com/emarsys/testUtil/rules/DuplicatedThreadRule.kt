package com.emarsys.testUtil.rules


import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class DuplicatedThreadExtension(private val threadName: String = "CoreSDKHandlerThread") :
    BeforeEachCallback {

    override fun beforeEach(p0: ExtensionContext?) {
        val threads =
            Thread.getAllStackTraces().keys.map { it.name }.filter { it.startsWith(threadName) }
        if (threads.size > 1) {
            throw Throwable("TEST: $threadName thread is duplicated")
        }
    }


}