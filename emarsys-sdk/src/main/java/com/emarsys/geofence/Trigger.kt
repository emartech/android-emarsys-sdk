package com.emarsys.geofence

import org.json.JSONObject

data class Trigger(val id: String,
                   val type: Enum<TriggerType>,
                   val loiteringDelay: Int,
                   val action: JSONObject)

enum class TriggerType {
    ENTER,
    EXIT,
    DWELLING;
}