package com.emarsys.mobileengage.client

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed

class LoggingClientServiceInternal(private val klass: Class<*>) : ClientServiceInternal {
    override fun trackDeviceInfo(completionListener: CompletionListener?) {
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, null))
    }
}