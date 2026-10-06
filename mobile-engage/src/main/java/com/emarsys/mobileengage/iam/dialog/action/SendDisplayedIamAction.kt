package com.emarsys.mobileengage.iam.dialog.action

import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.event.EventServiceInternal

class SendDisplayedIamAction(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val eventServiceInternal: EventServiceInternal
) : OnDialogShownAction {

    override fun execute(campaignId: String, sid: String?, url: String?) {
        concurrentHandlerHolder.coreHandler.post {
            val attributes: MutableMap<String, String> = HashMap()
            attributes["campaignId"] = campaignId
            if (sid != null) {
                attributes["sid"] = sid
            }
            if (url != null) {
                attributes["url"] = url
            }
            val eventName = "inapp:viewed"
            eventServiceInternal.trackInternalCustomEventAsync(eventName, attributes, null)
        }
    }
}
