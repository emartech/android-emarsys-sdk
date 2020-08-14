package com.emarsys.eventservice

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.getDependency
import com.emarsys.mobileengage.event.EventServiceInternal

class EventService(private val loggingInstance: Boolean = false): EventServiceApi {
    override fun trackCustomEvent(eventName: String, eventAttributes: Map<String, String?>?, completionListener: CompletionListener?): String? {
        return (if (loggingInstance) getDependency("loggingInstance") else getDependency<EventServiceInternal>("defaultInstance"))
                .trackCustomEvent(eventName, eventAttributes, completionListener)
    }

    override fun trackCustomEventAsync(eventName: String, eventAttributes: Map<String, String?>?, completionListener: CompletionListener?) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<EventServiceInternal>("defaultInstance"))
                .trackCustomEventAsync(eventName, eventAttributes, completionListener)
    }

    override fun trackInternalCustomEvent(eventName: String, eventAttributes: Map<String, String?>?, completionListener: CompletionListener?): String? {
        return (if (loggingInstance) getDependency("loggingInstance") else getDependency<EventServiceInternal>("defaultInstance"))
                .trackInternalCustomEvent(eventName, eventAttributes, completionListener)
    }

    override fun trackInternalCustomEventAsync(eventName: String, eventAttributes: Map<String, String?>?, completionListener: CompletionListener?) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<EventServiceInternal>("defaultInstance"))
                .trackInternalCustomEventAsync(eventName, eventAttributes, completionListener)
    }
}