package com.emarsys.mobileengage.api.event

import android.content.Context
import org.json.JSONObject

@FunctionalInterface
interface EventHandler {
    fun handleEvent(context: Context, eventName: String, payload: JSONObject?)
}