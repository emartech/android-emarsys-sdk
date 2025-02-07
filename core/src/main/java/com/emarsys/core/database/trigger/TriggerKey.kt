package com.emarsys.core.database.trigger

class TriggerKey(val tableName: String, val triggerType: TriggerType, val triggerEvent: TriggerEvent) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as TriggerKey
        if (tableName != that.tableName) return false
        return if (triggerType != that.triggerType) false else triggerEvent === that.triggerEvent
    }

    override fun hashCode(): Int {
        var result = tableName.hashCode()
        result = 31 * result + triggerType.hashCode()
        result = 31 * result + triggerEvent.hashCode()
        return result
    }
}