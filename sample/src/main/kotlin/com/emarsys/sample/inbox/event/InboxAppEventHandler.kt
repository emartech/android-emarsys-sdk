package com.emarsys.sample.inbox.event

import android.content.Context
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.sample.ui.component.toast.customTextToast
import org.json.JSONObject

class InboxAppEventHandler: EventHandler {
    override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
        customTextToast(context, "$eventName - $payload")
    }
}