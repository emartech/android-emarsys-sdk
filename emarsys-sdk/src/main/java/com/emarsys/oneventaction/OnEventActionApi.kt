package com.emarsys.oneventaction

import android.content.Context
import com.emarsys.mobileengage.api.event.EventHandler
import org.json.JSONObject

interface OnEventActionApi {
    fun setOnEventActionEventHandler(eventHandler: EventHandler)
}