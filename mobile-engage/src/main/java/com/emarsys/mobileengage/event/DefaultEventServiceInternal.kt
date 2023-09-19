package com.emarsys.mobileengage.event

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
import com.emarsys.core.util.Assert
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory

@Mockable
class DefaultEventServiceInternal(
    private val requestModelFactory: MobileEngageRequestModelFactory,
    private val requestManager: RequestManager
) : EventServiceInternal {

    override fun trackCustomEvent(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    ): String? {
        Assert.notNull(eventName, "EventName must not be null!")
        val requestId = try {
            val requestModel =
                requestModelFactory.createCustomEventRequest(eventName, eventAttributes)
            requestManager.submit(requestModel, completionListener)
            requestModel.id
        } catch (e: IllegalArgumentException) {
            completionListener?.onCompleted(e)
            null
        }
        return requestId
    }

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
    ): String? {
        Assert.notNull(eventName, "EventName must not be null!")
        val requestId = try {
            val requestModel =
                requestModelFactory.createInternalCustomEventRequest(eventName, eventAttributes)
            requestManager.submit(requestModel, completionListener)
            requestModel.id
        } catch (e: IllegalArgumentException) {
            completionListener?.onCompleted(e)
            null
        }
        return requestId
    }

    override fun trackInternalCustomEventAsync(
        eventName: String,
        eventAttributes: Map<String, String>?,
        completionListener: CompletionListener?
    ) {
        trackInternalCustomEvent(eventName, eventAttributes, completionListener)
    }
}