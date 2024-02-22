package com.emarsys.mobileengage.iam.dialog.action

import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.util.Assert
import com.emarsys.mobileengage.event.EventServiceInternal

class SendDisplayedIamAction(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val eventServiceInternal: EventServiceInternal
) : OnDialogShownAction {

    override fun execute(campaignId: String, sid: String?, url: String?) {
        Assert.notNull(campaignId, "CampaignId must not be null!")
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
