package com.emarsys.core.device

import com.emarsys.core.database.repository.AbstractSqlSpecification

data class FilterByHardwareId(
        private val arg: String,
        override val selection: String = "hardware_id=?") : AbstractSqlSpecification() {

    override val selectionArgs: Array<String>
        get() = arrayOf(arg)
}