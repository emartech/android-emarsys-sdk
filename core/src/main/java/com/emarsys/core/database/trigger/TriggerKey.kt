package com.emarsys.core.database.trigger

class TriggerKey(val tableName: String, val triggerType: TriggerType, val triggerEvent: TriggerEvent) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as TriggerKey
        if (if (tableName != null) tableName != that.tableName else that.tableName != null) return false
        return if (triggerType != that.triggerType) false else triggerEvent === that.triggerEvent
    }

    override fun hashCode(): Int {
        var result = tableName.hashCode()
        result = 31 * result + triggerType.hashCode()
        result = 31 * result + triggerEvent.hashCode()
        return result
    }
}