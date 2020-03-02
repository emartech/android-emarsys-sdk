package com.emarsys.mobileengage.geofence.model

import org.json.JSONObject

data class Trigger(val id: String,
                   val type: Enum<TriggerType>,
                   val loiteringDelay: Int = 0,
                   val action: JSONObject)

enum class TriggerType {
    ENTER,
    EXIT,
    DWELLING;
}