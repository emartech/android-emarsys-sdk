package com.emarsys.eventservice

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.di.mobileEngage

class EventService(private val loggingInstance: Boolean = false): EventServiceApi {
    override fun trackCustomEvent(eventName: String, eventAttributes: Map<String, String>?, completionListener: CompletionListener?): String? {
        return (if (loggingInstance) mobileEngage().loggingEventServiceInternal else mobileEngage().eventServiceInternal)
                .trackCustomEvent(eventName, eventAttributes, completionListener)
    }

    override fun trackCustomEventAsync(eventName: String, eventAttributes: Map<String, String>?, completionListener: CompletionListener?) {
        (if (loggingInstance) mobileEngage().loggingEventServiceInternal else mobileEngage().eventServiceInternal)
                .trackCustomEventAsync(eventName, eventAttributes, completionListener)
    }

    override fun trackInternalCustomEvent(eventName: String, eventAttributes: Map<String, String>?, completionListener: CompletionListener?): String? {
        return (if (loggingInstance) mobileEngage().loggingEventServiceInternal else mobileEngage().eventServiceInternal)
                .trackInternalCustomEvent(eventName, eventAttributes, completionListener)
    }

    override fun trackInternalCustomEventAsync(eventName: String, eventAttributes: Map<String, String>?, completionListener: CompletionListener?) {
        (if (loggingInstance) mobileEngage().loggingEventServiceInternal else mobileEngage().eventServiceInternal)
                .trackInternalCustomEventAsync(eventName, eventAttributes, completionListener)
    }
}