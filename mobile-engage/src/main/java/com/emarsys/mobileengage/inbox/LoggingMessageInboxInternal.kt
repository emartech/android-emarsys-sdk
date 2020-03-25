package com.emarsys.mobileengage.inbox

import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed
import com.emarsys.mobileengage.api.inbox.MessageInboxResult

class LoggingMessageInboxInternal(val klass: Class<*>) : MessageInboxInternal {
    override fun fetchInboxMessages(resultListener: ResultListener<Try<MessageInboxResult>>) {
        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, mapOf()))
    }
}