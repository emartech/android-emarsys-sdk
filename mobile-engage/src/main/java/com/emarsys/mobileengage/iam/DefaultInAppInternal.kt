package com.emarsys.mobileengage.iam

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.EventServiceInternal

class DefaultInAppInternal(
    private val inAppEventHandlerInternal: InAppEventHandlerInternal,
    private val eventServiceInternal: EventServiceInternal
) : InAppInternal {

    override fun pause() = inAppEventHandlerInternal.pause()

    override fun resume() = inAppEventHandlerInternal.resume()

    override val isPaused: Boolean get() = inAppEventHandlerInternal.isPaused

    override var eventHandler: EventHandler?
        get() = inAppEventHandlerInternal.eventHandler
        set(value) { inAppEventHandlerInternal.eventHandler = value }

    override fun trackCustomEvent(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    ): String? = eventServiceInternal.trackCustomEvent(eventName, eventAttributes, completionListener)

    override fun trackCustomEventAsync(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    ) {
        trackCustomEvent(eventName, eventAttributes, completionListener)
    }

    override fun trackInternalCustomEvent(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    ): String? = eventServiceInternal.trackInternalCustomEvent(eventName, eventAttributes, completionListener)

    override fun trackInternalCustomEventAsync(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    ) {
        trackInternalCustomEvent(eventName, eventAttributes, completionListener)
    }
}
