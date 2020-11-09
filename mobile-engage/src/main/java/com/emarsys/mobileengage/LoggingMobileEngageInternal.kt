package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed

class LoggingMobileEngageInternal(private val klass: Class<*>) : MobileEngageInternal {
    override fun setContact(contactFieldValue: String, completionListener: CompletionListener?) {
        val parameters = mapOf(
                "contact_field_value" to contactFieldValue,
                "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun clearContact(completionListener: CompletionListener?) {
        val parameters = mapOf(
                "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }
}