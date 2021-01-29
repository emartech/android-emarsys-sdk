package com.emarsys.core.device

import com.emarsys.core.database.repository.AbstractSqlSpecification

data class FilterByHardwareId(private val arg: String, private val selection: String = "hardware_id=?") : AbstractSqlSpecification() {

    override fun getSelection(): String {
        return selection
    }

    override fun getSelectionArgs(): Array<String> {
        return arrayOf(arg)
    }
}