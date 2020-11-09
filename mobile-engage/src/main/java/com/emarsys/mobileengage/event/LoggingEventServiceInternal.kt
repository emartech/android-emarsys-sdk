package com.emarsys.mobileengage.event

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed

class LoggingEventServiceInternal(private val klass: Class<*>) : EventServiceInternal {
    override fun trackCustomEvent(eventName: String, eventAttributes: Map<String, String>, completionListener: CompletionListener?): String? {
        val parameters = mapOf(
                "event_name" to eventName,
                "event_attributes" to eventAttributes,
                "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return null
    }

    override fun trackInternalCustomEvent(eventName: String, eventAttributes: Map<String, String>, completionListener: CompletionListener?): String? {
        val parameters = mapOf(
                "event_name" to eventName,
                "event_attributes" to eventAttributes,
                "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return null
    }

    override fun trackCustomEventAsync(eventName: String, eventAttributes: Map<String, String>, completionListener: CompletionListener?) {
        val parameters = mapOf(
                "event_name" to eventName,
                "event_attributes" to eventAttributes,
                "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun trackInternalCustomEventAsync(eventName: String, eventAttributes: Map<String, String>, completionListener: CompletionListener?) {
        val parameters = mapOf(
                "event_name" to eventName,
                "event_attributes" to eventAttributes,
                "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }
}