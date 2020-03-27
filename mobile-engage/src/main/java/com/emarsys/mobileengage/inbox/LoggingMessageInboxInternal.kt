package com.emarsys.mobileengage.inbox

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed
import com.emarsys.mobileengage.api.inbox.InboxResult

class LoggingMessageInboxInternal(val klass: Class<*>) : MessageInboxInternal {
    override fun fetchMessages(resultListener: ResultListener<Try<InboxResult>>) {
        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, mapOf()))
    }

    override fun addTag(tag: String, messageId: String, completionListener: CompletionListener?) {
        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, mapOf(
                "tag" to tag,
                "message_id" to messageId,
                "completion_listener" to (completionListener != null)
        )))
    }

    override fun removeTag(tag: String, messageId: String, completionListener: CompletionListener?) {
        val callerMethodName = SystemUtils.getCallerMethodName()

        debug(MethodNotAllowed(klass, callerMethodName, mapOf(
                "tag" to tag,
                "message_id" to messageId,
                "completion_listener" to (completionListener != null)
        )))
    }
}