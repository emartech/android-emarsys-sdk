package com.emarsys.mobileengage.event

import com.emarsys.core.api.result.CompletionListener

interface EventServiceInternal {
    fun trackCustomEvent(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    ): String?

    fun trackCustomEventAsync(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    )

    fun trackInternalCustomEvent(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    ): String?

    fun trackInternalCustomEventAsync(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    )
}