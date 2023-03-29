package com.emarsys.mobileengage.iam

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed
import com.emarsys.mobileengage.api.event.EventHandler

class LoggingInAppInternal(private val klass: Class<*>) : InAppInternal {
    override fun trackCustomEvent(eventName: String, eventAttributes: Map<String, String>?, completionListener: CompletionListener?): String? {
        val parameters = mapOf(
                "event_name" to eventName,
                "event_attributes" to eventAttributes,
                "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return null
    }

    override fun trackInternalCustomEvent(eventName: String, eventAttributes: Map<String, String>?, completionListener: CompletionListener?): String? {
        val parameters = mapOf(
                "event_name" to eventName,
                "event_attributes" to eventAttributes,
                "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return null
    }

    override fun trackCustomEventAsync(eventName: String, eventAttributes: Map<String, String>?, completionListener: CompletionListener?) {
        val parameters = mapOf(
                "event_name" to eventName,
                "event_attributes" to eventAttributes,
                "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun trackInternalCustomEventAsync(eventName: String, eventAttributes: Map<String, String>?, completionListener: CompletionListener?) {
        val parameters = mapOf(
                "event_name" to eventName,
                "event_attributes" to eventAttributes,
                "completion_listener" to (completionListener != null)
        )

        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun pause() {
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, null))
    }

    override fun resume() {
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, null))
    }

    override val isPaused: Boolean
        get() {
            val callerMethodName = SystemUtils.getCallerMethodName()
            debug(MethodNotAllowed(klass, callerMethodName, null))
            return false
        }
    override var eventHandler: EventHandler?
        get() {
            val callerMethodName = SystemUtils.getCallerMethodName()
            debug(MethodNotAllowed(klass, callerMethodName, null))
            return null
        }
        set(value) {
            val parameters = mapOf(
                    "event_handler" to (value != null)
            )
            val callerMethodName = SystemUtils.getCallerMethodName()
            debug(MethodNotAllowed(klass, callerMethodName, parameters))

        }
}